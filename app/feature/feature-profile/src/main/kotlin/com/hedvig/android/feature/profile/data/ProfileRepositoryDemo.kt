package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.apollo.OperationResult

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

  override suspend fun profile(): Either<OperationResult.Error, ProfileData> = either {
    ProfileData(member = demoMember)
  }

  override suspend fun updateEmail(input: String): Either<OperationResult.Error, ProfileData.Member> = either {
    email = input
    demoMember
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, ProfileData.Member> = either {
    phoneNumber = input
    demoMember
  }

  override suspend fun updateEmailSubscriptionPreference(subscribe: Boolean) {
  }
}
