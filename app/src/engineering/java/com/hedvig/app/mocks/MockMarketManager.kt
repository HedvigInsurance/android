package com.hedvig.app.mocks

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class MockMarketManager : MarketManager {
    override val enabledMarkets
        get() = mockedEnabledMarkets

    override var market: Market?
        get() = mockedMarket
        set(value) {
            mockedMarket = value
        }

    override var hasSelectedMarket: Boolean = true

    companion object {
        var mockedMarket: Market? = null
        var mockedEnabledMarkets = Market.values().toList()
    }
}
