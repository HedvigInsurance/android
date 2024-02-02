package com.hedvig.android.market

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

interface MarketManager {
  /**
   * The current market, defaulting to [Market.SE] if none is set, for cases wherer we need a best guess on the market,
   * in a synchronous manner.
   */
  val market: StateFlow<Market>

  /**
   * Suspend version which does not default to some market if none is set.
   */
  fun selectedMarket(): Flow<Market?>
}

internal class MarketManagerImpl(
  private val marketStorage: MarketStorage,
  coroutineScope: CoroutineScope,
) : MarketManager {
  @Deprecated("Try to use selectedMarket instead, which does not default to SE by itself")
  override val market: StateFlow<Market> = marketStorage.market
    .filterNotNull()
    .stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      Market.SE,
    )

  override fun selectedMarket(): Flow<Market?> {
    return marketStorage.market
  }
}
