package com.hedvig.android.market

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

interface MarketManager {
  val market: StateFlow<Market>
}

internal class MarketManagerImpl(
  private val marketStorage: MarketStorage,
  coroutineScope: CoroutineScope,
) : MarketManager {
  override val market: StateFlow<Market> = marketStorage.market.stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    Market.SE,
  )
}
