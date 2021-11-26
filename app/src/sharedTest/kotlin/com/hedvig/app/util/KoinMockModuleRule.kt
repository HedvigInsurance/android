package com.hedvig.app.util

import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

class KoinMockModuleRule(
    private val original: List<Module>,
    private val mocks: List<Module>
) : ExternalResource() {
    override fun before() {
        unloadKoinModules(original)
        loadKoinModules(mocks)
    }

    override fun after() {
        unloadKoinModules(mocks)
        loadKoinModules(original)
    }
}
