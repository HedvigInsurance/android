package com.hedvig.app.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeWatch
import com.hedvig.android.apollo.toEither
import giraffe.ProfileQuery
import giraffe.UpdateEmailMutation
import giraffe.UpdatePhoneNumberMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal interface ProfileRepository {
  fun profile(): Flow<Either<OperationResult.Error, ProfileQuery.Data>>
  suspend fun updateEmail(input: String): ApolloResponse<UpdateEmailMutation.Data>
  suspend fun updatePhoneNumber(input: String): ApolloResponse<UpdatePhoneNumberMutation.Data>
  suspend fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?)
}

internal class ProfileRepositoryImpl(
  private val apolloClient: ApolloClient,
) : ProfileRepository {
  private val profileQuery = ProfileQuery()

  override fun profile(): Flow<Either<OperationResult.Error, ProfileQuery.Data>> = apolloClient
    .query(profileQuery)
    .safeWatch()
    .map(OperationResult<ProfileQuery.Data>::toEither)

  override suspend fun updateEmail(input: String) =
    apolloClient.mutation(UpdateEmailMutation(input)).execute()

  override suspend fun updatePhoneNumber(input: String) =
    apolloClient.mutation(UpdatePhoneNumberMutation(input)).execute()

  override suspend fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
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
