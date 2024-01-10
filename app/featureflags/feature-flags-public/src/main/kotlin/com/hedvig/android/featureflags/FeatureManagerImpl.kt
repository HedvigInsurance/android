package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.FeatureFlagProvider

internal class FeatureManagerImpl(
  private val featureFlagProvider: FeatureFlagProvider,
) : FeatureManager, FeatureFlagProvider by featureFlagProvider
