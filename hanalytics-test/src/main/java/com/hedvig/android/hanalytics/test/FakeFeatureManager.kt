package com.hedvig.android.hanalytics.test

import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.hanalytics.LoginMethod
import com.hedvig.hanalytics.PaymentType

class FakeFeatureManager(
  private val featureMap: (() -> Map<Feature, Boolean>)? = null,
  private val loginMethod: (() -> LoginMethod)? = null,
  private val paymentType: (() -> PaymentType)? = null,
) : FeatureManager {
  override suspend fun invalidateExperiments() {}

  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    val featureMap = featureMap?.invoke() ?: error("Set the featureMap returned from FakeFeatureManager")
    return featureMap[feature] ?: error("Set a return value for feature:$feature on FakeFeatureManager")
  }

  override suspend fun getLoginMethod(): LoginMethod {
    return loginMethod?.invoke() ?: error("Set the loginMethod returned from FakeFeatureManager")
  }

  override suspend fun getPaymentType(): PaymentType {
    return paymentType?.invoke() ?: error("Set the paymentType returned from FakeFeatureManager")
  }

  companion object {
    fun withSomePaymentType(): FakeFeatureManager {
      return FakeFeatureManager(
        paymentType = { PaymentType.ADYEN },
      )
    }
  }
}
