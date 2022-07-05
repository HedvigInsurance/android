package com.hedvig.app.util.featureflags.paymenttype

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType

class HAnalyticsPaymentTypeProvider(
  private val hAnalytics: HAnalytics,
) : PaymentTypeProvider {
  override suspend fun getPaymentType(): PaymentType {
    return hAnalytics.paymentType()
  }
}
