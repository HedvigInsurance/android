package com.hedvig.app.util

import com.hedvig.app.feature.offer.OfferPersistenceManager
import com.hedvig.app.offerPersistenceManagerModule
import io.mockk.every
import io.mockk.mockk
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class MockedOfferPersistenceManagerRule : ExternalResource() {

    private val mockedOfferPersistenceManager = mockk<OfferPersistenceManager>(relaxed = true)

    override fun before() {
        unloadKoinModules(offerPersistenceManagerModule)
        loadKoinModules(module { single { mockedOfferPersistenceManager } })
        every { mockedOfferPersistenceManager.getPersistedQuoteIds() } returns setOf("testId")
    }

    override fun after() {
        unloadKoinModules(module { single { mockedOfferPersistenceManager } })
        loadKoinModules(offerPersistenceManagerModule)
    }
}
