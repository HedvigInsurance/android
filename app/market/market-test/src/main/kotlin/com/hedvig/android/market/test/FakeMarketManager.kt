package com.hedvig.android.market.test

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeMarketManager(
  initialMarket: Market = Market.SE,
) : MarketManager {
  override val market: StateFlow<Market> = MutableStateFlow(initialMarket).asStateFlow()

  override fun selectedMarket(): Flow<Market?> {
    error("Not yet implemented")
  }
}
