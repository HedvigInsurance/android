package com.hedvig.android.feature.movingflow

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data class SelectContractForMovingKey(
  val source: MovingSource = MovingSource.OTHER,
) : HedvigNavKey

/**
 * Where the user entered the moving flow from. Reported to the backend when the move quotes are requested.
 */
@Serializable
enum class MovingSource {
  INSURANCE,
  TERMINATION,
  OTHER,
}
