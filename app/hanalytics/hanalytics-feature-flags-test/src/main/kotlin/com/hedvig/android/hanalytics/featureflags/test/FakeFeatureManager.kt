package com.hedvig.android.hanalytics.featureflags.test

import app.cash.turbine.Turbine
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
    /**
     * A FeatureManager which just returns some value for each feature, should be used when the result doesn't matter
     * but the class being tested does need a FeatureManager.
     */
    operator fun invoke(noopFeatureManager: Boolean): FakeFeatureManager {
      return if (noopFeatureManager) {
        FakeFeatureManager(
          { Feature.values().toList().associateWith { false } },
          { LoginMethod.OTP },
          { PaymentType.ADYEN },
        )
      } else {
        error("Use the normal constructor instead")
      }
    }
  }
}

/**
 * Uses a Turbine for the feature, to have more control over what and when it returns a value during a test.
 * Should probably delete `FakeFeatureManager` asap in favor of this, while keeping same API maybe.
 */
class FakeFeatureManager2(
  private val fixedMap: Map<Feature, Boolean> = emptyMap(),
) : FeatureManager {
  val featureTurbine = Turbine<Pair<Feature, Boolean>>(name = "FeatureTurbine")

  override suspend fun invalidateExperiments() {}

  override suspend fun isFeatureEnabled(feature: Feature): Boolean {
    if (fixedMap.containsKey(feature)) {
      return fixedMap.get(feature)!!
    }
    val pair = featureTurbine.awaitItem()
    require(feature == pair.first)
    return pair.second
  }

  override suspend fun getLoginMethod(): LoginMethod {
    error("Not supported")
  }

  override suspend fun getPaymentType(): PaymentType {
    error("Not supported")
  }
}
