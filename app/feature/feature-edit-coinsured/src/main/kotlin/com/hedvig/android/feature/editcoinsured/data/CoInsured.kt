package com.hedvig.android.feature.editcoinsured.data

import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

internal data class CoInsured(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val ssn: String?,
  val hasMissingInfo: Boolean,
  val internalId: String = UUID.randomUUID().toString(),
) {
  val id = "$firstName-$lastName-$birthDate-$ssn"

  val displayName: String = formatName(firstName, lastName)

  fun identifier(dateTimeFormatter: DateTimeFormatter): String? {
    return if (ssn != null) {
      formatSsn(ssn)
    } else {
      birthDate?.toJavaLocalDate()?.format(dateTimeFormatter)
    }
  }
}
