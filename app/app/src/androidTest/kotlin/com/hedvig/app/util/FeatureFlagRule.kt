package com.hedvig.app.util

import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.di.featureManagerModule
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.hanalytics.PaymentType
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class FeatureFlagRule(
  vararg flags: Pair<Feature, Boolean>,
  private val paymentType: PaymentType = PaymentType.TRUSTLY,
) : ExternalResource() {
  private val mockFeatureManagerModule = module {
    single<FeatureManager> {
      FakeFeatureManager(
        featureMap = { flags.toMap() },
        paymentType = { paymentType },
      )
    }
  }

  override fun before() {
    unloadKoinModules(featureManagerModule)
    loadKoinModules(mockFeatureManagerModule)
  }

  override fun after() {
    unloadKoinModules(mockFeatureManagerModule)
    loadKoinModules(featureManagerModule)
  }
}
