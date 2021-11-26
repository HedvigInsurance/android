package com.hedvig.app.util

import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.valueStoreModule
import io.mockk.every
import io.mockk.mockk
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class ValueStoreRule(
    private val key: String,
    private val value: String,
) : ExternalResource() {

    private val valueStore = mockk<ValueStore>(relaxed = true)

    override fun before() {
        unloadKoinModules(valueStoreModule)
        loadKoinModules(module { single { valueStore } })
        every { valueStore.get(key) } returns value
    }

    override fun after() {
        unloadKoinModules(module { single { valueStore } })
        loadKoinModules(valueStoreModule)
    }
}
