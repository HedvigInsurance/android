package com.hedvig.android.feature.editcoinsured.data

import kotlinx.datetime.LocalDate

data class CoInsured(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val ssn: String?,
  val hasMissingInfo: Boolean,
)
