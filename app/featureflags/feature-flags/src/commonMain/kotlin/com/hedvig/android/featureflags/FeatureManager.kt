package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface FeatureManager {
  @NativeCoroutines
  fun isFeatureEnabled(feature: Feature): Flow<Boolean>

  /**
   * Suspends until flag values are available for the current member, or for no member while logged out,
   * whether freshly fetched from the backend or restored from the last fetch's local cache. A decision that
   * must honor the flag, for example a kill switch gating a whole flow, should await this and treat a
   * failure to complete as "no value available yet". Without values for this member, for example offline
   * and never fetched for them, this never completes; callers must impose their own timeout.
   */
  suspend fun awaitReady()
}
