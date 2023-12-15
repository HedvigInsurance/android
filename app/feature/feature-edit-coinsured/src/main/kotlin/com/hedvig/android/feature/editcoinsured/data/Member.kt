package com.hedvig.android.feature.editcoinsured.data

import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn

internal data class Member(
  val firstName: String,
  val lastName: String,
  val ssn: String?,
) {
  val displayName: String = formatName(firstName, lastName)

  fun identifier(): String? {
    return if (ssn != null) {
      formatSsn(ssn)
    } else {
      null
    }
  }
}
