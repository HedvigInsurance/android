package com.hedvig.android.featureflags.flags

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.HedvigUnleashClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class UnleashFeatureFlagProvider(
  private val hedvigUnleashClient: HedvigUnleashClient,
) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    // Each Feature's name mirrors the polarity of its underlying Unleash key (enable_* / disable_*),
    // so the raw toggle value is the feature value. Callers of a disable_* flag invert at the read site.
    return hedvigUnleashClient.featureUpdatedFlow
      .map { hedvigUnleashClient.client.isEnabled(feature.unleashKey) }
      .distinctUntilChanged()
  }
}
