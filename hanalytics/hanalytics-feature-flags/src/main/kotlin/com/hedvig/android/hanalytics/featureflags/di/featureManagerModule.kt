package com.hedvig.android.hanalytics.featureflags.di

import com.hedvig.android.core.common.android.di.isDebugQualifier
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.FeatureManagerImpl
import com.hedvig.android.hanalytics.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.HAnalyticsFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.DevLoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.HAnalyticsLoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.DevPaymentTypeProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.HAnalyticsPaymentTypeProvider
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val featureManagerModule = module {
  single<ClearHAnalyticsExperimentsCacheUseCase> { ClearHAnalyticsExperimentsCacheUseCase(get()) }
  single<FeatureManager> {
    if (get(isDebugQualifier)) {
      FeatureManagerImpl(
        DevFeatureFlagProvider(get()),
        DevLoginMethodProvider(get()),
        DevPaymentTypeProvider(get()),
        get<ClearHAnalyticsExperimentsCacheUseCase>(),
      )
    } else {
      FeatureManagerImpl(
        HAnalyticsFeatureFlagProvider(get()),
        HAnalyticsLoginMethodProvider(get()),
        HAnalyticsPaymentTypeProvider(get()),
        get<ClearHAnalyticsExperimentsCacheUseCase>(),
      )
    }
  }
}
