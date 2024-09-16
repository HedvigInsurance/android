package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either

internal class ProfileRepositoryDemo : ProfileRepository {
  private var email = "google@gmail.com"
  private var phoneNumber = "072102103"

  private val demoMember: ProfileData.Member
    get() = ProfileData.Member(
      id = "test",
      firstName = "Google",
      lastName = "Tester",
      phoneNumber = phoneNumber,
      email = email,
    )

  override suspend fun profile(): Either<Unit, ProfileData> = either {
    ProfileData(member = demoMember)
  }

  override suspend fun updateEmail(input: String): Either<Unit, ProfileData.Member> = either {
    email = input
    demoMember
  }

  override suspend fun updatePhoneNumber(input: String): Either<Unit, ProfileData.Member> = either {
    phoneNumber = input
    demoMember
  }
}
