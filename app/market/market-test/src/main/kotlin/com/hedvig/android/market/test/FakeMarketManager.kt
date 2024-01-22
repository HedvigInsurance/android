package com.hedvig.android.market.test

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeMarketManager(
  private val initialMarket: Market = Market.SE,
) : MarketManager {
  val _market: MutableStateFlow<Market> = MutableStateFlow(initialMarket)
  override val market: StateFlow<Market> = _market.asStateFlow()

  override suspend fun market(): Market? {
    error("Not yet implemented")
  }
}
