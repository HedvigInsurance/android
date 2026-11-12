package com.hedvig.android.featureflags.flags

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.JvmUnleashClient
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class JvmUnleashFeatureFlagProvider(
  private val unleashClient: JvmUnleashClient,
) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return unleashClient.featureUpdatedFlow
      .map { unleashClient.valueOf(feature) }
      .distinctUntilChanged()
  }

  override suspend fun awaitReady() {
    unleashClient.awaitReady()
  }
}
