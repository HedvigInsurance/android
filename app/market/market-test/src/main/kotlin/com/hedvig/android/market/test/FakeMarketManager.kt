package com.hedvig.android.market.test

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

class FakeMarketManager(
  private val _market: Market,
) : MarketManager {
  override val enabledMarkets: List<Market>
    get() = TODO()

  override var market: Market?
    get() = _market
    set(_) {
      error("Construct FakeMarketManager with another market instead")
    }
}
