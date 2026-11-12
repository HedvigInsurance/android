package com.hedvig.android.feature.profile.data

internal data class ProfileData(
  val member: Member,
) {
  data class Member(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
  )
}
