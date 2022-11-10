package com.hedvig.app.mocks

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

class MockMarketManager : MarketManager {
  override val enabledMarkets
    get() = mockedEnabledMarkets

  override var market: Market?
    get() = mockedMarket
    set(value) {
      mockedMarket = value
    }

  companion object {
    var mockedMarket: Market? = null
    var mockedEnabledMarkets = Market.values().toList()
  }
}
