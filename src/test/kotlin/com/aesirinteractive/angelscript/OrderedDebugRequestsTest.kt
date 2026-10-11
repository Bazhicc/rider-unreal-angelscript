package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class OrderedDebugRequestsTest {
    @Test fun routesSameExpressionToItsOriginalFrameAndCallback() {
        val scheduler = ScheduledThreadPoolExecutor(1)
        try {
            val sent = mutableListOf<Int>()
            val results = mutableListOf<String>()
            val queue = OrderedDebugRequests<String>(scheduler, { fail("Unexpected timeout") })
            for (frame in listOf(0, 1, 0)) queue.submit({ sent.add(frame); true },
                { results.add("$frame:$it") }, { fail(it) })
            assertEquals(listOf(0), sent)
            queue.receive("top")
            queue.receive("caller")
            queue.receive("top again")
            assertEquals(listOf(0, 1, 0), sent)
            assertEquals(listOf("0:top", "1:caller", "0:top again"), results)
        } finally { scheduler.shutdownNow() }
    }

    @Test fun discardsLateReplyAfterResumeBeforeSendingNewSuspensionRequest() {
        val scheduler = ScheduledThreadPoolExecutor(1)
        try {
            val sent = mutableListOf<String>()
            val results = mutableListOf<String>()
            val errors = mutableListOf<String>()
            val queue = OrderedDebugRequests<String>(scheduler, { fail("Unexpected timeout") })
            fun submit(name: String) = queue.submit({ sent.add(name); true }, { results.add("$name:$it") }, { errors.add(name) })
            submit("old in flight")
            submit("old queued")
            queue.invalidate("Execution resumed")
            submit("new frame")
            assertEquals(listOf("old in flight"), sent)
            queue.receive("stale")
            queue.receive("fresh")
            assertEquals(listOf("old in flight", "new frame"), sent)
            assertEquals(listOf("new frame:fresh"), results)
            assertEquals(listOf("old in flight", "old queued"), errors)
        } finally { scheduler.shutdownNow() }
    }

    @Test fun timeoutRejectsRequestsUntilTransportIsReset() {
        val scheduler = ScheduledThreadPoolExecutor(1)
        try {
            val timedOut = CountDownLatch(1)
            val errors = java.util.concurrent.CopyOnWriteArrayList<String>()
            val queue = OrderedDebugRequests<String>(scheduler, { timedOut.countDown() }, 30)
            queue.submit({ true }, { fail("Timed out reply") }, { errors.add(it) })
            assertTrue(timedOut.await(3, TimeUnit.SECONDS))
            queue.submit({ fail("Must not send on a desynchronized connection"); true }, {}, { errors.add(it) })
            assertEquals(listOf("Debugger request timed out", "Debugger is disconnected"), errors)
            queue.reset("Reconnected", true)
            var result = ""
            queue.submit({ true }, { result = it }, { fail(it) })
            queue.receive("fresh")
            assertEquals("fresh", result)
        } finally { scheduler.shutdownNow() }
    }

    @Test fun failedSendAndDisconnectReleaseCallbacks() {
        val scheduler = ScheduledThreadPoolExecutor(1)
        try {
            val errors = mutableListOf<String>()
            val queue = OrderedDebugRequests<String>(scheduler, { fail("Unexpected timeout") })
            queue.submit({ false }, { fail("Unsent request") }, { errors.add(it) })
            queue.submit({ true }, { fail("Disconnected request") }, { errors.add(it) })
            queue.reset("Stopped", false)
            queue.receive("late")
            assertEquals(listOf("Debugger is disconnected", "Stopped"), errors)
        } finally { scheduler.shutdownNow() }
    }
}
