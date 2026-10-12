package com.hedvig.android.feature.editcoinsured.data

import com.benasher44.uuid.uuid4
import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import com.hedvig.android.design.system.hedvig.DateFormatter
import kotlinx.datetime.LocalDate

internal data class CoInsured(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val ssn: String?,
  val hasMissingInfo: Boolean,
  val activatesOn: LocalDate?,
  val terminatesOn: LocalDate?,
  val internalId: String = uuid4().toString(),
) {
  val id = "$firstName-$lastName-$birthDate-$ssn"

  val displayName: String = formatName(firstName, lastName)

  fun identifier(dateTimeFormatter: DateFormatter): String? {
    return if (ssn != null) {
      formatSsn(ssn)
    } else {
      birthDate ?: return null
      dateTimeFormatter.format(birthDate)
    }
  }
}
