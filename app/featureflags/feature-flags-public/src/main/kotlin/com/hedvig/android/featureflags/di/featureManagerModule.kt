package com.hedvig.android.featureflags.di

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.HedvigUnleashClient
import com.hedvig.android.featureflags.flags.UnleashFeatureFlagProvider
import com.hedvig.android.market.MarketManager
import org.koin.dsl.module

val featureManagerModule = module {
  single<HedvigUnleashClient> {
    HedvigUnleashClient(
      isProduction = get<HedvigBuildConstants>().isProduction,
      appVersionName = get<HedvigBuildConstants>().appVersionName,
      marketManager = get<MarketManager>(),
      coroutineScope = get<ApplicationScope>(),
      memberIdService = get<MemberIdService>(),
    )
  }

  single<FeatureManager> {
    if (get<HedvigBuildConstants>().isProduction) {
      UnleashFeatureFlagProvider(get<HedvigUnleashClient>())
    } else {
      UnleashFeatureFlagProvider(get<HedvigUnleashClient>())
    }
    // todo: don't understand this here
  }
}
