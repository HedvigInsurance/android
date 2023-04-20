package com.hedvig.app.feature.adyen

import com.hedvig.android.market.Market

enum class AdyenCurrency {
  NOK,
  DKK,
  ;

  companion object {
    fun fromMarket(market: Market) = when (market) {
      Market.NO -> NOK
      Market.DK -> DKK
      else -> error("Market $market is not supported by Adyen")
    }
  }
}
