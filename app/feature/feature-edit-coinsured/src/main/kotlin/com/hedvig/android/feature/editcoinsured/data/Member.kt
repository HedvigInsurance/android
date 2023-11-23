package com.hedvig.android.feature.editcoinsured.data

internal data class Member(
  val firstName: String,
  val lastName: String,
  val ssn: String?
) {
  val displayName: String = "$firstName $lastName"
}
