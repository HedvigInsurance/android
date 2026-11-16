package com.hedvig.android.umbrella

import com.hedvig.android.feature.claimhistory.ios.ClaimHistoryScreenModel
import com.hedvig.android.feature.claimhistory.ios.ClaimHistoryScreenState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.StateFlow

/**
 * [ClaimHistoryScreenModel.states] in the shape KMP-NativeCoroutines exports. It does not own [model]: the caller keeps
 * calling `reload()` and `close()` on the model itself.
 */
class ClaimHistoryScreenModelFlows(private val model: ClaimHistoryScreenModel) {
  @NativeCoroutinesState
  val state: StateFlow<ClaimHistoryScreenState>
    get() = model.states
}
