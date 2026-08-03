package com.hedvig.android.feature.movingflow

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data class SelectContractForMovingKey (
  val source: MovingSource = MovingSource.OTHER,
): HedvigNavKey

enum class MovingSource {
  INSURANCE,
  TERMINATION,
  OTHER
}
