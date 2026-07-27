package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface FeatureManager {
  @NativeCoroutines
  fun isFeatureEnabled(feature: Feature): Flow<Boolean>

  /**
   * Suspends until a real flag value is available for the current member, sourced from either the
   * on-disk backup of the last fetch or a fresh network fetch. Before this completes, [isFeatureEnabled]
   * reports only pre-fetch defaults, so a decision that must honor the remote value (for example a kill
   * switch gating a whole flow) should await this first. On a fresh install that is fully offline with no
   * backup this never completes, so callers must impose their own timeout and fall back to defaults.
   */
  suspend fun awaitReady()
}
