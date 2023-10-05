package com.hedvig.android.hanalytics.featureflags.paymenttype

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.hanalytics.PaymentType

internal class DevPaymentTypeProvider(
  private val marketManager: MarketManager,
) : PaymentTypeProvider {
  override suspend fun getPaymentType(): PaymentType {
    return when (marketManager.market.value) {
      Market.SE -> PaymentType.TRUSTLY
      Market.NO -> PaymentType.ADYEN
      Market.DK -> PaymentType.ADYEN
    }
  }
}
