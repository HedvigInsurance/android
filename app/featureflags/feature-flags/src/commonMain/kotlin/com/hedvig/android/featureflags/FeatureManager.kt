package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface FeatureManager {
  @NativeCoroutines
  fun isFeatureEnabled(feature: Feature): Flow<Boolean>

  /**
   * Suspends until the flag values are confirmed authoritative for the current session: the backend has
   * been reached and its current values are in effect, not a stale local fallback or a pre-fetch default.
   * A decision that must honor the remote value, for example a kill switch gating a whole flow, should
   * await this and treat a failure to complete as "value unknown". While the backend is unreachable
   * (offline, or a backend outage) this never completes, so callers must impose their own timeout.
   */
  suspend fun awaitFlagsFromServer()
}
