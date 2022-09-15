package com.hedvig.app.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.ProfileQuery
import com.hedvig.android.apollo.graphql.UpdateEmailMutation
import com.hedvig.android.apollo.graphql.UpdatePhoneNumberMutation
import com.hedvig.android.apollo.safeWatch
import com.hedvig.android.apollo.toEither
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
  private val apolloClient: ApolloClient,
) {
  private val profileQuery = ProfileQuery()

  fun profile(): Flow<Either<OperationResult.Error, ProfileQuery.Data>> = apolloClient
    .query(profileQuery)
    .safeWatch()
    .map(OperationResult<ProfileQuery.Data>::toEither)

  suspend fun updateEmail(input: String) =
    apolloClient.mutation(UpdateEmailMutation(input)).execute()

  suspend fun updatePhoneNumber(input: String) =
    apolloClient.mutation(UpdatePhoneNumberMutation(input)).execute()

  suspend fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
    val cachedData = apolloClient
      .apolloStore
      .readOperation(profileQuery)
    val newMember = cachedData
      .member
      .copy(
        email = email,
        phoneNumber = phoneNumber,
      )

    val newData = cachedData
      .copy(member = newMember)

    apolloClient
      .apolloStore
      .writeOperation(profileQuery, newData)
  }
}
