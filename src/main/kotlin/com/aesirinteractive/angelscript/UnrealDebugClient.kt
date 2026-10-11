package com.aesirinteractive.angelscript

import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

enum class MessageType(val id: Int) {
    Diagnostics(0),
    RequestDebugDatabase(1),
    DebugDatabase(2),
    StartDebugging(3),
    StopDebugging(4),
    Pause(5),
    Continue(6),
    RequestCallStack(7),
    CallStack(8),
    ClearBreakpoints(9),
    SetBreakpoint(10),
    HasStopped(11),
    HasContinued(12),
    StepOver(13),
    StepIn(14),
    StepOut(15),
    EngineBreak(16),
    RequestVariables(17),
    Variables(18),
    RequestEvaluate(19),
    Evaluate(20),
    GoToDefinition(21),
    BreakOptions(22),
    RequestBreakFilters(23),
    BreakFilters(24),
    Disconnect(25),
    DebugDatabaseFinished(26),
    AssetDatabaseInit(27),
    AssetDatabase(28),
    AssetDatabaseFinished(29),
    FindAssets(30),
    DebugDatabaseSettings(31),
    PingAlive(32),
    DebugServerVersion(33),
    CreateBlueprint(34),
    ReplaceAssetDefinition(35),
    SetDataBreakpoints(36),
    ClearDataBreakpoints(37),
    ScriptRecompiled(38),
    StopPIE(39);

    companion object {
        fun fromId(id: Int): MessageType? = entries.find { it.id == id }
    }
}

data class BreakFilter(val name: String, val title: String)

data class CallStackFrame(val name: String, val sourcePath: String, val line: Int, val moduleName: String)

data class DebugVariable(
    val name: String,
    val value: String,
    val type: String,
    val hasMembers: Boolean
)

class IncomingMessage(val type: Int, private val data: ByteArray) {
    private var cursor = 0

    private fun requireBytes(count: Long) {
        if (count < 0 || count > data.size - cursor) throw IOException("Truncated UE debug message")
    }

    fun readInt(): Int {
        requireBytes(4)
        val buf = ByteBuffer.wrap(data, cursor, 4).order(ByteOrder.LITTLE_ENDIAN)
        cursor += 4
        return buf.int
    }

    fun readByte(): Int {
        requireBytes(1)
        val v = data[cursor].toInt() and 0xFF
        cursor += 1
        return v
    }

    fun readBool(): Boolean = readInt() != 0

    fun readString(): String {
        val n = readInt()
        val ucs2 = n < 0
        val count = if (ucs2) -n.toLong() else n.toLong()
        if (count == 0L) return ""
        requireBytes(count * if (ucs2) 2 else 1)

        val str = if (ucs2) {
            val byteCount = (count * 2).toInt()
            val s = String(data, cursor, byteCount, Charset.forName("UTF-16LE"))
            cursor += byteCount
            s
        } else {
            val s = String(data, cursor, count.toInt(), Charsets.UTF_8)
            cursor += count.toInt()
            s
        }
        // Strip null terminator
        return if (str.isNotEmpty() && str.last() == '\u0000') str.dropLast(1) else str
    }

    fun hasMore(): Boolean = cursor < data.size
}

/** Contiguous storage with a read cursor; consuming a frame never shifts every pending byte. */
internal class UnrealMessageBuffer {
    private var bytes = ByteArray(4096)
    private var start = 0
    private var end = 0

    fun append(data: ByteArray, length: Int): List<IncomingMessage> {
        require(length in 0..data.size)
        val pending = end - start
        if (bytes.size - end < length) {
            if (bytes.size < pending + length) bytes = bytes.copyOf(maxOf(bytes.size * 2, pending + length))
            bytes.copyInto(bytes, 0, start, end)
            start = 0
            end = pending
        }
        data.copyInto(bytes, end, 0, length)
        end += length
        val messages = mutableListOf<IncomingMessage>()
        while (end - start >= 5) {
            val size = ByteBuffer.wrap(bytes, start, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (size !in 0..MAX_PAYLOAD) throw IOException("Invalid UE debug frame length: $size")
            if (end - start < size + 5) break
            messages.add(IncomingMessage(bytes[start + 4].toInt() and 0xff, bytes.copyOfRange(start + 5, start + 5 + size)))
            start += size + 5
        }
        if (start == end) { start = 0; end = 0 }
        return messages
    }

    companion object { private const val MAX_PAYLOAD = 64 * 1024 * 1024 }
}

class UnrealDebugClient(val host: String = "127.0.0.1", val port: Int = 27099) {

    // Callbacks — set by AngelscriptProcess before connecting
    var onStopped: ((reason: String, description: String, text: String) -> Unit)? = null
    var onContinued: (() -> Unit)? = null
    var onCallStack: ((frames: List<CallStackFrame>) -> Unit)? = null
    var onVariables: ((variables: List<DebugVariable>) -> Unit)? = null
    var onServerVersion: ((version: Int) -> Unit)? = null
    var onBreakFilters: ((filters: List<BreakFilter>) -> Unit)? = null
    var onEvaluate: ((name: String, value: String, type: String, hasMembers: Boolean) -> Unit)? = null
    var onClosed: (() -> Unit)? = null
    var onBreakpointAdjusted: ((id: Int, path: String, adjustedLine: Int) -> Unit)? = null
    var onScriptRecompiled: (() -> Unit)? = null
    var onMessageLog: ((direction: String, type: String) -> Unit)? = null

    @Volatile var serverVersion: Int = 0
    @Volatile var isConnected: Boolean = false

    @Volatile private var socket: Socket? = null
    @Volatile private var outputStream: OutputStream? = null
    private var readerThread: Thread? = null
    private var connectThread: Thread? = null

    private val sendLock = Any()
    private val lifecycleLock = Any()
    private var connectionGeneration = 0L

    /**
     * Connect asynchronously with retry. Retries every [retryDelayMs] ms up to [maxRetries] times.
     * Pass [maxRetries] = -1 for unlimited retries (never calls [onFailed]).
     * Calls [onConnected] when the connection succeeds.
     */
    fun connectWithRetry(
        maxRetries: Int = 30,
        retryDelayMs: Long = 2000L,
        onConnected: (() -> Unit)? = null,
        onRetry: ((attempt: Int) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        disconnect()
        val generation = synchronized(lifecycleLock) { connectionGeneration }
        val unlimited = maxRetries < 0
        val thread = Thread({
            var attempt = 0
            while (true) {
                attempt++
                if (Thread.currentThread().isInterrupted) return@Thread
                val s = Socket()
                try {
                    synchronized(lifecycleLock) {
                        if (generation != connectionGeneration) { s.close(); return@Thread }
                        socket = s // disconnect can also cancel a connection in progress.
                    }
                    s.connect(InetSocketAddress(host, port), 3000)
                    synchronized(lifecycleLock) {
                        if (generation != connectionGeneration) { s.close(); return@Thread }
                        outputStream = s.getOutputStream()
                        serverVersion = 0
                        isConnected = true
                        onConnected?.invoke()
                        if (generation == connectionGeneration) startReaderThread(s, generation)
                    }
                    return@Thread
                } catch (_: IOException) {
                    try { s.close() } catch (_: IOException) {}
                    if (generation != synchronized(lifecycleLock) { connectionGeneration }) return@Thread
                    onRetry?.invoke(attempt)
                    if (!unlimited && attempt >= maxRetries) break
                    try { Thread.sleep(retryDelayMs) } catch (_: InterruptedException) { return@Thread }
                }
            }
            synchronized(lifecycleLock) {
                if (generation == connectionGeneration) onFailed?.invoke()
            }
        }, "AngelScript-Connect").also { it.isDaemon = true }
        synchronized(lifecycleLock) {
            if (generation == connectionGeneration) { connectThread = thread; thread.start() }
        }
    }

    fun disconnect() {
        synchronized(lifecycleLock) {
            connectionGeneration++
            isConnected = false
            connectThread?.interrupt()
            readerThread?.interrupt()
            try { socket?.close() } catch (_: IOException) {}
            socket = null
            outputStream = null
            connectThread = null
            readerThread = null
        }
    }

    private fun startReaderThread(s: Socket, generation: Long) {
        readerThread = Thread({
            val buf = ByteArray(4096)
            val pending = UnrealMessageBuffer()
            try {
                val inputStream = s.getInputStream()
                while (!Thread.currentThread().isInterrupted) {
                    val read = inputStream.read(buf)
                    if (read == -1) break
                    for (message in pending.append(buf, read)) {
                        synchronized(lifecycleLock) {
                            if (generation != connectionGeneration) return@Thread
                            dispatchMessage(message)
                        }
                    }
                }
            } catch (_: IOException) {
                // socket closed
            } finally {
                try { s.close() } catch (_: IOException) {}
                synchronized(lifecycleLock) {
                    if (generation == connectionGeneration) {
                        isConnected = false
                        socket = null
                        outputStream = null
                        onClosed?.invoke()
                    }
                }
            }
        }, "AngelScript-Reader").also { it.isDaemon = true; it.start() }
    }

    private fun dispatchMessage(msg: IncomingMessage) {
        val msgType = MessageType.fromId(msg.type)
        val typeName = msgType?.name ?: "Unknown(${msg.type})"
        if (msgType != MessageType.SetBreakpoint) onMessageLog?.invoke("<--", typeName)
        when (msgType) {
            MessageType.HasStopped -> {
                val reason = msg.readString()
                val description = msg.readString()
                val text = msg.readString()
                onStopped?.invoke(reason, description, text)
            }
            MessageType.HasContinued -> {
                onContinued?.invoke()
            }
            MessageType.CallStack -> {
                val count = msg.readInt()
                val frames = mutableListOf<CallStackFrame>()
                for (i in 0 until count) {
                    val name = msg.readString()
                    val path = msg.readString()
                    val line = msg.readInt()
                    val module = if (serverVersion > 0) msg.readString() else ""
                    frames.add(CallStackFrame(name, path, line, module))
                }
                onCallStack?.invoke(frames)
            }
            MessageType.Variables -> {
                val count = msg.readInt()
                val vars = mutableListOf<DebugVariable>()
                for (i in 0 until count) {
                    val name = msg.readString()
                    val value = msg.readString()
                    val type = msg.readString()
                    val hasMembers = msg.readBool()
                    if (serverVersion >= 2) {
                        // Skip 8-byte address + 1-byte size (not used in UI yet)
                        repeat(9) { msg.readByte() }
                    }
                    vars.add(DebugVariable(name, value, type, hasMembers))
                }
                onVariables?.invoke(vars)
            }
            MessageType.DebugServerVersion -> {
                serverVersion = msg.readInt()
                onServerVersion?.invoke(serverVersion)
            }
            MessageType.BreakFilters -> {
                val count = msg.readInt()
                val filters = mutableListOf<BreakFilter>()
                for (i in 0 until count) {
                    val name = msg.readString()
                    val title = msg.readString()
                    filters.add(BreakFilter(name, title))
                }
                onBreakFilters?.invoke(filters)
            }
            MessageType.Evaluate -> {
                val name = msg.readString()
                val value = msg.readString()
                val type = msg.readString()
                val hasMembers = msg.readBool()
                onEvaluate?.invoke(name, value, type, hasMembers)
            }
            MessageType.SetBreakpoint -> {
                val path = msg.readString()
                val line = msg.readInt()
                val id   = msg.readInt()
                onMessageLog?.invoke("<--", "SetBreakpoint id=$id path=$path line=$line")
                onBreakpointAdjusted?.invoke(id, path, line)
            }
            MessageType.ScriptRecompiled -> {
                onScriptRecompiled?.invoke()
            }
            else -> { /* ignore unknown/unhandled messages */ }
        }
    }

    // ── Send helpers ──────────────────────────────────────────────────────────

    private fun writeInt(value: Int): ByteArray {
        val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(value)
        return buf.array()
    }

    private fun writeString(str: String): ByteArray {
        val unicode = str.any { it.code > 127 }
        val strBytes = (str + "\u0000").toByteArray(if (unicode) Charsets.UTF_16LE else Charsets.US_ASCII)
        val lenBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        lenBuf.putInt(if (unicode) -(str.length + 1) else strBytes.size)
        return lenBuf.array() + strBytes
    }

    private fun buildSimpleMessage(type: MessageType): ByteArray {
        val buf = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(1) // length = 1 (just the type byte)
        buf.put(type.id.toByte())
        return buf.array()
    }

    private fun buildMessage(type: MessageType, payload: ByteArray): ByteArray {
        val length = 1 + payload.size // type byte + payload
        val header = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(length)
        header.put(type.id.toByte())
        return header.array() + payload
    }

    private fun send(data: ByteArray, logDetail: String? = null): Boolean {
        if (data.size >= 5) {
            val typeId = data[4].toInt() and 0xFF
            val typeName = MessageType.fromId(typeId)?.name ?: "Unknown($typeId)"
            onMessageLog?.invoke("-->", if (logDetail != null) "$typeName $logDetail" else typeName)
        }
        return synchronized(sendLock) {
            val stream = outputStream ?: return@synchronized false
            try {
                stream.write(data)
                stream.flush()
                true
            } catch (_: IOException) {
                // Closing the socket wakes the reader, which reports connection loss once.
                try { socket?.close() } catch (_: IOException) {}
                false
            }
        }
    }

    // ── Public send methods ───────────────────────────────────────────────────

    fun sendStartDebugging(version: Int = 2) {
        send(buildMessage(MessageType.StartDebugging, writeInt(version)))
    }

    fun sendStopDebugging() = send(buildSimpleMessage(MessageType.StopDebugging))

    fun sendDisconnect() = send(buildSimpleMessage(MessageType.Disconnect))

    fun sendContinue() = send(buildSimpleMessage(MessageType.Continue))

    fun sendPause() = send(buildSimpleMessage(MessageType.Pause))

    fun sendStepOver() = send(buildSimpleMessage(MessageType.StepOver))

    fun sendStepIn() = send(buildSimpleMessage(MessageType.StepIn))

    fun sendStepOut() = send(buildSimpleMessage(MessageType.StepOut))

    fun sendRequestCallStack() = send(buildSimpleMessage(MessageType.RequestCallStack))

    fun sendRequestBreakFilters() = send(buildSimpleMessage(MessageType.RequestBreakFilters))

    fun sendRequestVariables(path: String): Boolean =
        send(buildMessage(MessageType.RequestVariables, writeString(path)))

    fun sendRequestEvaluate(expression: String, frameId: Int): Boolean {
        val payload = writeString(expression) + writeInt(frameId)
        return send(buildMessage(MessageType.RequestEvaluate, payload))
    }

    fun sendSetBreakpoint(id: Int, path: String, line: Int, moduleName: String) {
        val payload = writeString(path) + writeInt(line) + writeInt(id) + writeString(moduleName)
        send(buildMessage(MessageType.SetBreakpoint, payload), "id=$id path=$path line=$line")
    }

    fun sendClearBreakpoints(path: String, moduleName: String) {
        val payload = writeString(path) + writeString(moduleName)
        send(buildMessage(MessageType.ClearBreakpoints, payload), "path=$path")
    }

    fun sendBreakOptions(filters: List<String>) {
        var payload = writeInt(filters.size)
        for (f in filters) payload += writeString(f)
        send(buildMessage(MessageType.BreakOptions, payload))
    }

    fun sendStopPIE() = send(buildSimpleMessage(MessageType.StopPIE))
}
