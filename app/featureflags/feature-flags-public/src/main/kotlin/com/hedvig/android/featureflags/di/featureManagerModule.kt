package com.hedvig.android.featureflags.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.FeatureManagerImpl
import com.hedvig.android.featureflags.UnleashClientBuilder
import com.hedvig.android.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.android.featureflags.flags.FeatureFlagProvider
import com.hedvig.android.featureflags.flags.UnleashFeatureFlagProvider
import io.getunleash.UnleashClient
import org.koin.dsl.module

val featureManagerModule = module {
  single<UnleashClient> {
    UnleashClientBuilder(get<HedvigBuildConstants>().isDebug).client
  }

  single<FeatureManager> {
    val featureFlagProvider: FeatureFlagProvider = if (get<HedvigBuildConstants>().isDebug) {
      DevFeatureFlagProvider()
    } else {
      UnleashFeatureFlagProvider(get<UnleashClient>())
    }

    FeatureManagerImpl(
      featureFlagProvider,
    )
  }
}
