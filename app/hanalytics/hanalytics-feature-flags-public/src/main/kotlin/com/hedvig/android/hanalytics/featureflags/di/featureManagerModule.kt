package com.hedvig.android.hanalytics.featureflags.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.FeatureManagerImpl
import com.hedvig.android.hanalytics.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagAuthEventListener
import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.HAnalyticsFeatureFlagProvider
import com.hedvig.android.market.MarketManager
import com.hedvig.hanalytics.HAnalytics
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val featureManagerModule = module {
  single<ClearHAnalyticsExperimentsCacheUseCase> { ClearHAnalyticsExperimentsCacheUseCase(get()) }
  single<FeatureManager> {
    val featureFlagProvider: FeatureFlagProvider = if (get<HedvigBuildConstants>().isDebug) {
      DevFeatureFlagProvider(get<MarketManager>())
    } else {
      HAnalyticsFeatureFlagProvider(get<HAnalytics>())
    }
    FeatureManagerImpl(
      featureFlagProvider,
      get<ClearHAnalyticsExperimentsCacheUseCase>(),
    )
  }
  single<FeatureFlagAuthEventListener> {
    FeatureFlagAuthEventListener(get<ClearHAnalyticsExperimentsCacheUseCase>())
  } bind AuthEventListener::class
}
