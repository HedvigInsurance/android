package com.hedvig.app.mocks

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockMarketManager : MarketManager {
  override val enabledMarkets
    get() = mockedEnabledMarkets

  override var market: Market?
    get() = mockedMarket
    set(value) {
      mockedMarket = value
    }

  override suspend fun setMarket(market: Market) {
    this.market = market
  }

  override suspend fun removeMarket() {
    this.market = null
  }

  override fun observeMarket(): Flow<Market?> {
    return flowOf(market)
  }

  companion object {
    var mockedMarket: Market? = null
    var mockedEnabledMarkets = Market.values().toList()
  }
}
