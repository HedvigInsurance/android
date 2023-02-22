package com.hedvig.android.hanalytics.featureflags.paymenttype

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal class DevPaymentTypeProvider(
  private val marketManager: MarketManager,
) : PaymentTypeProvider {

  override suspend fun getPaymentType(): PaymentType {
    return marketManager.observeMarket().first().let { market ->
      when (market) {
        Market.SE -> PaymentType.TRUSTLY
        Market.NO -> PaymentType.ADYEN
        Market.DK -> PaymentType.ADYEN
        Market.FR -> throw IllegalArgumentException()
        null -> PaymentType.TRUSTLY
      }
    }
  }
}
