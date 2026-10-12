package com.aesirinteractive.angelscript

import com.google.gson.Gson
import com.sun.net.httpserver.HttpServer
import org.junit.Assert.*
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Real C# SDK adapter, HTTP request and Kotlin/Gson DTOs; no Rider backend solution is mocked here. */
class AngelscriptNativeTransportTest {
    @Test fun nativeBackendAndKotlinExchangeDeclarationAndUsageCoordinates() {
        val gson = Gson()
        val received = AtomicReference<AngelscriptNativeUsageQuery>()
        val expected = AngelscriptNativeUsageRange("F:/Script 中文/Probe.as", 18, 22, 1, 5, 1, 9)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/references") { exchange -> exchange.use {
            assertEquals("POST", it.requestMethod)
            assertEquals("Bearer test-token", it.requestHeaders.getFirst("Authorization"))
            received.set(gson.fromJson(it.requestBody.reader().readText(), AngelscriptNativeUsageQuery::class.java))
            val body = gson.toJson(AngelscriptNativeUsageResponse(listOf(expected))).toByteArray(Charsets.UTF_8)
            it.sendResponseHeaders(200, body.size.toLong())
            it.responseBody.write(body)
        } }
        server.start()
        try {
            val process = ProcessBuilder("dotnet", System.getProperty("angelscript.test.backendHarness"),
                System.getProperty("angelscript.test.riderBackend"), "http://127.0.0.1:${server.address.port}/references", "test-token")
                .redirectErrorStream(true).start()
            try {
                assertTrue("Backend transport timed out", process.waitFor(15, TimeUnit.SECONDS))
                val output = process.inputStream.reader().readText().trim()
                assertEquals(output, 0, process.exitValue())
                assertEquals(listOf(expected), gson.fromJson(output, AngelscriptNativeUsageResponse::class.java).usages)
                assertEquals(AngelscriptNativeUsageQuery("NativeCall", listOf(AngelscriptNativeDeclaration("F:/Native 中文/Probe.h", 17, 1, 4))), received.get())
            } finally { if (process.isAlive) process.destroy() }
        } finally { server.stop(0) }
    }
}
