package com.hedvig.android.feature.movingflow

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data class SelectContractForMovingKey(
  val source: MovingSource = MovingSource.OTHER,
) : HedvigNavKey

/**
 * Where the user entered the moving flow from. Reported to the backend when the move quotes are requested.
 *
 * [TERMINATION] is the only entry point the backend treats differently; [INSURANCE] and [OTHER] both end up recorded
 * as an insurance-initiated move. Pick [OTHER] for an entry point that is neither the insurance screens nor the
 * termination flow, so that the distinction stays available if the backend starts modelling it.
 */
@Serializable
enum class MovingSource {
  INSURANCE,
  TERMINATION,
  OTHER,
}
