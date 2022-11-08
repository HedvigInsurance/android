package com.hedvig.app.feature.adyen

import com.hedvig.android.market.Market
import java.lang.IllegalArgumentException

enum class AdyenCurrency {
  NOK,
  DKK;

  companion object {
    fun fromMarket(market: Market) = when (market) {
      Market.NO -> NOK
      Market.DK -> DKK
      else -> throw IllegalArgumentException("Market $market is not supported by Adyen")
    }
  }
}
