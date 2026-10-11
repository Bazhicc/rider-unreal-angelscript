package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.DataInputStream
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class UnrealDebugProtocolTest {
    @Test fun buffersLargeAndCoalescedFramesWithoutLosingFragmentBoundaries() {
        val payload = ByteArray(256 * 1024) { (it % 251).toByte() }
        val large = ByteBuffer.allocate(payload.size + 5).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(payload.size).put(127).put(payload).array()
        val small = byteArrayOf(0, 0, 0, 0, MessageType.HasContinued.id.toByte())
        val stream = large + small + small
        val buffer = UnrealMessageBuffer()
        val messages = stream.asList().chunked(4093).flatMap { chunk ->
            val bytes = chunk.toByteArray()
            buffer.append(bytes, bytes.size)
        }
        assertEquals(listOf(127, 12, 12), messages.map { it.type })
        for (byte in payload) assertEquals(byte.toInt() and 0xff, messages[0].readByte())
        assertFalse(messages[0].hasMore())
    }

    @Test fun rejectsInvalidFramesAndTruncatedStrings() {
        for (size in listOf(-1, Int.MAX_VALUE)) {
            val bytes = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN).putInt(size).put(20).array()
            assertThrows(java.io.IOException::class.java) { UnrealMessageBuffer().append(bytes, bytes.size) }
        }
        for (count in listOf(100, -100, Int.MIN_VALUE)) {
            val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(count).array()
            assertThrows(java.io.IOException::class.java) { IncomingMessage(20, bytes).readString() }
        }
    }

    @Test fun explicitDisconnectAndReplacementDoNotReportOldConnectionLoss() {
        ServerSocket(0).use { server ->
            server.soTimeout = 4000
            val client = UnrealDebugClient("127.0.0.1", server.localPort)
            val closed = CountDownLatch(1)
            client.onClosed = { closed.countDown() }
            try {
                val first = CountDownLatch(1)
                client.connectWithRetry(maxRetries = 1, onConnected = { first.countDown() })
                server.accept().use { old ->
                    old.soTimeout = 4000
                    assertTrue(first.await(4, TimeUnit.SECONDS))
                    client.disconnect()
                    assertEquals(-1, old.getInputStream().read())
                    val second = CountDownLatch(1)
                    client.connectWithRetry(maxRetries = 1, onConnected = { second.countDown() })
                    server.accept().use { current ->
                        assertTrue(second.await(4, TimeUnit.SECONDS))
                        assertTrue(client.isConnected)
                        current.close()
                        assertTrue(closed.await(4, TimeUnit.SECONDS))
                        assertFalse(client.isConnected)
                    }
                }
            } finally { client.disconnect() }
        }
    }

    @Test fun sendsUnicodeBreakpointPathAndHandlesFragmentedReplies() {
        ServerSocket(0).use { server ->
            server.soTimeout = 4000
            val client = UnrealDebugClient("127.0.0.1", server.localPort)
            val connected = CountDownLatch(1)
            val continued = CountDownLatch(1)
            client.onContinued = { continued.countDown() }
            try {
                client.connectWithRetry(maxRetries = 1, onConnected = { connected.countDown() })
                server.accept().use { socket ->
                    socket.soTimeout = 4000
                    assertTrue(connected.await(4, TimeUnit.SECONDS))
                    client.sendSetBreakpoint(3, "Script/角色.as", 12, "角色")
                    val input = DataInputStream(socket.getInputStream())
                    val length = Integer.reverseBytes(input.readInt())
                    val type = input.readUnsignedByte()
                    assertEquals(MessageType.SetBreakpoint.id, type)
                    val payload = ByteArray(length - 1)
                    input.readFully(payload)
                    val message = IncomingMessage(type, payload)
                    assertEquals("Script/角色.as", message.readString())
                    assertEquals(12, message.readInt())
                    assertEquals(3, message.readInt())
                    assertEquals("角色", message.readString())
                    assertFalse(message.hasMore())
                    val reply = byteArrayOf(0, 0, 0, 0, MessageType.HasContinued.id.toByte())
                    socket.getOutputStream().write(reply, 0, 2)
                    socket.getOutputStream().flush()
                    socket.getOutputStream().write(reply, 2, 3)
                    socket.getOutputStream().flush()
                    assertTrue(continued.await(4, TimeUnit.SECONDS))
                }
            } finally {
                client.disconnect()
            }
        }
    }

    @Test fun readsUnrealUnicodeStringAndFollowingInteger() {
        val text = "Script/角色.as"
        val bytes = (text + '\u0000').toByteArray(Charsets.UTF_16LE)
        val packet = ByteBuffer.allocate(8 + bytes.size).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(-(text.length + 1)).put(bytes).putInt(42).array()
        val message = IncomingMessage(MessageType.CallStack.id, packet)
        assertEquals(text, message.readString())
        assertEquals(42, message.readInt())
        assertFalse(message.hasMore())
    }

    @Test fun readsEmptyStringsWithoutConsumingNextValue() {
        val packet = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).putInt(7).array()
        val message = IncomingMessage(MessageType.Evaluate.id, packet)
        assertEquals("", message.readString())
        assertEquals(7, message.readInt())
    }
}
