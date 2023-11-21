package com.hedvig.android.feature.editcoinsured.data

import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

data class CoInsured(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val ssn: String?,
  val hasMissingInfo: Boolean,
) {
  val displayName: String? = if (firstName != null && lastName != null) {
    "$firstName $lastName"
  } else {
    null
  }

  fun details(dateTimeFormatter: DateTimeFormatter): String? =
    ssn ?: birthDate?.toJavaLocalDate()?.format(dateTimeFormatter)
}
