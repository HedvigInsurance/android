package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.hedvig.android.apollo.OperationResult

internal interface ProfileRepository {
  suspend fun profile(): Either<OperationResult.Error, ProfileData>

  suspend fun updateEmail(input: String): Either<OperationResult.Error, ProfileData.Member>

  suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, ProfileData.Member>

  suspend fun updateEmailSubscriptionPreference(subscribe: Boolean)
}
