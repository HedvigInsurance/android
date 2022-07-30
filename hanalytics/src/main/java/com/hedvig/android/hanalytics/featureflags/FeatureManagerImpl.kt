package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.LoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.PaymentTypeProvider

internal class FeatureManagerImpl(
  private val featureFlagProvider: FeatureFlagProvider,
  private val loginMethodProvider: LoginMethodProvider,
  private val paymentTypeProvider: PaymentTypeProvider,
  private val clearHAnalyticsExperimentsCacheUseCase: ClearHAnalyticsExperimentsCacheUseCase,
) : FeatureManager,
  FeatureFlagProvider by featureFlagProvider,
  LoginMethodProvider by loginMethodProvider,
  PaymentTypeProvider by paymentTypeProvider {

  override suspend fun invalidateExperiments() {
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }
}
