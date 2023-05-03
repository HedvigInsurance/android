package com.example.feature.changeaddress.data

import kotlinx.datetime.LocalDate

@JvmInline
value class MoveIntentId(val id: String)

data class MoveIntent(
  val id: MoveIntentId,
  val currentHomeAddresses: List<Address>,
  val movingDateRange: ClosedRange<LocalDate>,
  val numberCoInsured: Int,
)
