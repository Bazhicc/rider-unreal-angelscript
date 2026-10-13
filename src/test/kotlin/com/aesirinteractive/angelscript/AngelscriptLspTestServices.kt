package com.aesirinteractive.angelscript

import com.intellij.mock.MockProject
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import java.lang.reflect.Proxy

/** Register the service behind getInstance() on both sides of the 2026.1.4 API rename. */
internal fun registerLspTestManager(project: MockProject, servers: () -> Collection<LspServer>) {
    @Suppress("UNCHECKED_CAST")
    val managerClass = try {
        Class.forName("com.intellij.platform.lsp.api.LspClientManager")
    } catch (_: ClassNotFoundException) {
        LspServerManager::class.java
    } as Class<Any>
    // New SDKs register LspClientManager, whose implementation also implements the legacy interface.
    val manager = Proxy.newProxyInstance(managerClass.classLoader, arrayOf(LspServerManager::class.java)) { _, method, _ ->
        check(method.name == "getServersForProvider") {
            "Unexpected LSP manager access: ${method.name}"
        }
        servers()
    }
    project.picoContainer.unregisterComponent(managerClass.name)
    project.registerService(managerClass, manager)
}
