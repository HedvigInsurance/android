package com.hedvig.android.market

@InternalHedvigMarketApi
interface InternalSetMarketUseCase {
  suspend fun setMarket(market: Market)
}

@InternalHedvigMarketApi
internal class InternalSetMarketUseCaseImpl(
  private val marketStorage: MarketStorage,
) : InternalSetMarketUseCase {
  override suspend fun setMarket(market: Market) {
    marketStorage.setMarket(market)
  }
}
