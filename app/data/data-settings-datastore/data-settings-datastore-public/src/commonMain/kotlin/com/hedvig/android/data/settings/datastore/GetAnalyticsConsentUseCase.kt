package com.hedvig.android.data.settings.datastore

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The single read path for the member's analytics consent, so every screen that surfaces the
 * decision is gated by the same kill switch instead of each one remembering to check the flag.
 */
interface GetAnalyticsConsentUseCase {
  /**
   * The member's [AnalyticsConsent], or null while [Feature.DISABLE_ANALYTICS] is on: the member is
   * never asked for a decision then, so there is nothing to show or act on.
   */
  fun invoke(): Flow<AnalyticsConsent?>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetAnalyticsConsentUseCaseImpl(
  private val settingsDataStore: SettingsDataStore,
  private val featureManager: FeatureManager,
) : GetAnalyticsConsentUseCase {
  override fun invoke(): Flow<AnalyticsConsent?> {
    return combine(
      featureManager.isFeatureEnabled(Feature.DISABLE_ANALYTICS),
      settingsDataStore.observeAnalyticsConsent(),
    ) { analyticsDisabled, consent ->
      consent.takeIf { !analyticsDisabled }
    }
  }
}
