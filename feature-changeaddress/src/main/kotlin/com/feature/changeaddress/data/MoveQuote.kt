package com.feature.changeaddress.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class MoveQuote(
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: Double,
  val startDate: LocalDate,
  val termsVersion: String,
)
