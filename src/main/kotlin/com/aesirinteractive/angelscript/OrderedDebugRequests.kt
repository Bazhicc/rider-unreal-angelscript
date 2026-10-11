package com.aesirinteractive.angelscript

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** UE replies have no request IDs. Only one request of each reply type may be in flight. */
internal class OrderedDebugRequests<T>(
    private val scheduler: ScheduledExecutorService,
    private val onTimeout: () -> Unit,
    private val timeoutMs: Long = 5000,
) {
    private class Request<T>(
        val send: () -> Boolean,
        var result: ((T) -> Unit)?,
        var error: ((String) -> Unit)?,
    ) {
        var timer: ScheduledFuture<*>? = null
    }

    private val requests = ArrayDeque<Request<T>>()
    private var accepting = true

    @Synchronized fun submit(send: () -> Boolean, result: (T) -> Unit, error: (String) -> Unit) {
        if (!accepting) {
            error("Debugger is disconnected")
            return
        }
        requests.addLast(Request(send, result, error))
        if (requests.size == 1) sendHead()
    }

    @Synchronized fun receive(value: T) {
        val head = requests.removeFirstOrNull() ?: return
        head.timer?.cancel(false)
        head.result?.invoke(value)
        sendHead()
    }

    /** Keep the sent head as a tombstone so its late reply cannot consume a newer request. */
    @Synchronized fun invalidate(reason: String) {
        val head = requests.firstOrNull() ?: return
        requests.forEach { it.error?.invoke(reason); it.result = null; it.error = null }
        requests.clear()
        requests.addLast(head)
    }

    /** The transport must be closed before resetting, since old replies cannot be identified. */
    @Synchronized fun reset(reason: String, accepting: Boolean) {
        this.accepting = accepting
        requests.forEach { it.timer?.cancel(false); it.error?.invoke(reason) }
        requests.clear()
    }

    private fun sendHead() {
        while (requests.isNotEmpty()) {
            val head = requests.first()
            if (head.send()) {
                head.timer = scheduler.schedule({
                    val expired = synchronized(this) {
                        if (requests.firstOrNull() !== head) false
                        else { reset("Debugger request timed out", false); true }
                    }
                    // A timeout loses protocol alignment; reconnect before accepting more work.
                    if (expired) onTimeout()
                }, timeoutMs, TimeUnit.MILLISECONDS)
                return
            }
            requests.removeFirst()
            head.error?.invoke("Debugger is disconnected")
        }
    }
}
