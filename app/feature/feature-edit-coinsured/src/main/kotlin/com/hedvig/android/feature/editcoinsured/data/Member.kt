package com.hedvig.android.feature.editcoinsured.data

import com.hedvig.android.feature.editcoinsured.ui.formatSsn

internal data class Member(
  val firstName: String,
  val lastName: String,
  val ssn: String?,
) {
  val displayName: String = "$firstName $lastName"

  fun identifier(): String? {
    return if (ssn != null) {
      formatSsn(ssn)
    } else {
      null
    }
  }
}
