package com.hedvig.android.hanalytics.featureflags.paymenttype

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType

internal class HAnalyticsPaymentTypeProvider(
  private val hAnalytics: HAnalytics,
) : PaymentTypeProvider {
  override suspend fun getPaymentType(): PaymentType {
    return hAnalytics.paymentType()
  }
}
