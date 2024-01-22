package com.hedvig.android.market

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
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
  suspend fun market(): Market?
}

internal class MarketManagerImpl(
  private val marketStorage: MarketStorage,
  coroutineScope: CoroutineScope,
) : MarketManager {
  override val market: StateFlow<Market> = marketStorage.market
    .filterNotNull()
    .stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      Market.SE,
    )

  override suspend fun market(): Market? {
    return marketStorage.market.firstOrNull()
  }
}
