package com.hedvig.app.mocks

import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider

class MockMarketProvider : MarketProvider() {
    override val market
        get() = mockedMarket

    override val enabledMarkets
        get() = mockedEnabledMarkets

    companion object {
        var mockedMarket: Market? = null
        var mockedEnabledMarkets = Market.values().toList()
    }
}
