package com.hedvig.app.util.featureflags

import com.hedvig.app.util.featureflags.flags.Feature
import com.hedvig.hanalytics.LoginMethod
import com.hedvig.hanalytics.PaymentType

class FakeFeatureManager(
  private val featureMap: Map<Feature, Boolean> = emptyMap(),
  private val loginMethod: LoginMethod = LoginMethod.BANK_ID_SWEDEN,
  private val paymentType: PaymentType = PaymentType.ADYEN,
) : FeatureManager {
  override suspend fun invalidateExperiments() {}

  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    return featureMap.getOrDefault(feature, false)
  }

  override suspend fun getLoginMethod(): LoginMethod {
    return loginMethod
  }

  override suspend fun getPaymentType(): PaymentType {
    return paymentType
  }
}
