package com.hedvig.android.market

interface SetMarketUseCase {
  suspend fun setMarket(market: Market)
}

internal class SetMarketUseCaseImpl(
  private val marketStorage: MarketStorage,
) : SetMarketUseCase {
  override suspend fun setMarket(market: Market) {
    marketStorage.setMarket(market)
  }
}
