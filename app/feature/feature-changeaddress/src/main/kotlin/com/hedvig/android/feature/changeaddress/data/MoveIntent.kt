package com.hedvig.android.feature.changeaddress.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class MoveIntentId(val id: String)

data class MoveIntent(
  val id: MoveIntentId,
  val currentHomeAddresses: List<Address>,
  val movingDateRange: ClosedRange<LocalDate>,
  val numberCoInsured: Int,
)
