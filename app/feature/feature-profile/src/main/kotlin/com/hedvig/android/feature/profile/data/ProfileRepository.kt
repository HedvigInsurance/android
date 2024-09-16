package com.hedvig.android.feature.profile.data

import arrow.core.Either

internal interface ProfileRepository {
  suspend fun profile(): Either<Unit, ProfileData>

  suspend fun updateEmail(input: String): Either<Unit, ProfileData.Member>

  suspend fun updatePhoneNumber(input: String): Either<Unit, ProfileData.Member>
}
