package com.aesirinteractive.angelscript

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.testFramework.ApplicationRule
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import org.junit.Assert.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class AngelscriptNativeReadTest {
    @get:Rule val application = ApplicationRule()
    private val lifetime = Disposer.newDisposable()
    @After fun dispose() { Disposer.dispose(lifetime) }

    @Test fun pendingWriteCancelsNativeReadAndRetriesAfterWrite() {
        val started = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val attempts = AtomicInteger()
        val worker = AppExecutorUtil.getAppExecutorService().submit<String> {
            nativeUsageRead(lifetime) {
                attempts.incrementAndGet()
                started.countDown()
                while (finish.count != 0L) { ProgressManager.checkCanceled(); Thread.sleep(1) }
                "done"
            }
        }
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS))
            ApplicationManager.getApplication().invokeAndWait {
                // Start the deadlock guard after EDT dispatch. Shared CI runner
                // scheduling is not part of the read-action cancellation contract.
                val watchdog = AppExecutorUtil.getAppScheduledExecutorService()
                    .schedule({ finish.countDown() }, 3, TimeUnit.SECONDS)
                try {
                    ApplicationManager.getApplication().runWriteAction { finish.countDown() }
                } finally { watchdog.cancel(false) }
            }
            assertEquals("done", worker.get(5, TimeUnit.SECONDS))
            assertTrue("The cancelled read must restart after the write", attempts.get() > 1)
        } finally { finish.countDown(); worker.cancel(true) }
    }

    @Test fun timeoutCancelsTheRunningReadRatherThanLeavingItDetached() {
        val started = CountDownLatch(1)
        val stopped = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val worker = AppExecutorUtil.getAppExecutorService().submit<Boolean> {
            try {
                nativeUsageRead(lifetime, 200) {
                    started.countDown()
                    try {
                        while (finish.count != 0L) { ProgressManager.checkCanceled(); Thread.sleep(1) }
                        true
                    } finally { stopped.countDown() }
                }
                false
            } catch (_: TimeoutException) { true }
        }
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS))
            assertTrue(worker.get(5, TimeUnit.SECONDS))
            assertTrue("The read action must actually stop after its caller times out", stopped.await(2, TimeUnit.SECONDS))
            ApplicationManager.getApplication().invokeAndWait { ApplicationManager.getApplication().runWriteAction { } }
        } finally { finish.countDown(); worker.cancel(true) }
    }
}
