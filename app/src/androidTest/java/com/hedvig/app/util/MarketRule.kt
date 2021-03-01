package com.hedvig.app.util

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.marketManagerModule
import io.mockk.every
import io.mockk.mockk
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class MarketRule(val market: Market) : ExternalResource() {

    val marketManager = mockk<MarketManager>(relaxed = true)

    override fun before() {
        unloadKoinModules(marketManagerModule)
        loadKoinModules(module { single { marketManager } })
        every { marketManager.market } returns market
    }

    override fun after() {
        unloadKoinModules(module { single { marketManager } })
        loadKoinModules(marketManagerModule)
    }
}
