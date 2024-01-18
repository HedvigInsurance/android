package com.hedvig.android.featureflags.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.UnleashClientProvider
import com.hedvig.android.featureflags.flags.UnleashFeatureFlagProvider
import com.hedvig.android.market.MarketManager
import org.koin.dsl.module

val featureManagerModule = module {
  single<UnleashClientProvider> {
    UnleashClientProvider(
      isProduction = get<HedvigBuildConstants>().isProduction,
      appVersionName = get<HedvigBuildConstants>().appVersionName,
      marketManager = get<MarketManager>(),
      coroutineScope = get<ApplicationScope>(),
    )
  }

  single<FeatureManager> {
    if (get<HedvigBuildConstants>().isProduction) {
      UnleashFeatureFlagProvider(get<UnleashClientProvider>())
    } else {
      UnleashFeatureFlagProvider(get<UnleashClientProvider>())
    }
  }
}
