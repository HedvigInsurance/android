package com.hedvig.app.util.featureflags.paymenttype

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.hanalytics.PaymentType

class DevPaymentTypeProvider(
  private val marketManager: MarketManager,
) : PaymentTypeProvider {
  override suspend fun getPaymentType(): PaymentType {
    return when (marketManager.market) {
      Market.SE -> PaymentType.TRUSTLY
      Market.NO -> PaymentType.ADYEN
      Market.DK -> PaymentType.ADYEN
      Market.FR -> throw IllegalArgumentException()
      null -> PaymentType.TRUSTLY
    }
  }
}
