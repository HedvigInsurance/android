package com.example.feature.changeaddress.data

import kotlinx.datetime.LocalDate

data class MoveQuote(
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: Double,
  val startDate: LocalDate,
  val termsVersion: String,
)
