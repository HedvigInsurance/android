package com.hedvig.android.hanalytics.featureflags.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.FeatureManagerImpl
import com.hedvig.android.hanalytics.featureflags.UnleashClientBuilder
import com.hedvig.android.hanalytics.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagAuthEventListener
import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.UnleashFeatureFlagProvider
import com.hedvig.android.market.MarketManager
import io.getunleash.UnleashClient
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManagerModule = module {
  single<ClearHAnalyticsExperimentsCacheUseCase> { ClearHAnalyticsExperimentsCacheUseCase(get()) }
  single<UnleashClient> {
    UnleashClientBuilder(get<HedvigBuildConstants>().isDebug).client
  }
  single<FeatureManager> {
    val featureFlagProvider: FeatureFlagProvider = if (get<HedvigBuildConstants>().isDebug) {
      DevFeatureFlagProvider(get<MarketManager>())
    } else {
      UnleashFeatureFlagProvider(get<UnleashClient>())
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
