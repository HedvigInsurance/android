package com.hedvig.app.mocks

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager

class MockMarketManager : MarketManager {

    override val enabledMarkets
        get() = mockedEnabledMarkets

    override var market: Market? = mockedMarket

    override fun hasSelectedMarket(): Boolean {
        return true
    }

    companion object {
        var mockedMarket: Market? = null
        var mockedEnabledMarkets = Market.values().toList()
    }
}
