package com.hedvig.android.featureflags.di

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.NoopFeatureFlagProvider
import org.koin.dsl.module

actual val featureManagerModule = module {
  single<FeatureManager> {
    NoopFeatureFlagProvider()
  }
}
