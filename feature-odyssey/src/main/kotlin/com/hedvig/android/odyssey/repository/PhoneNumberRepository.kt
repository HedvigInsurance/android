package com.hedvig.android.odyssey.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import giraffe.ProfileQuery
import giraffe.UpdatePhoneNumberMutation

internal class PhoneNumberRepository(
  private val apolloClient: ApolloClient,
) {
  private val profileQuery = ProfileQuery()

  suspend fun getPhoneNumber(): PhoneNumberResult = apolloClient
    .query(profileQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .safeExecute()
    .let {
      when (it) {
        is OperationResult.Error -> PhoneNumberResult.Error(it.message ?: "Unknown error")
        is OperationResult.Success -> PhoneNumberResult.Success(it.data.member.phoneNumber ?: "")
      }
    }

  suspend fun updatePhoneNumber(input: String) =
    apolloClient.mutation(UpdatePhoneNumberMutation(input)).safeExecute()
}

sealed interface PhoneNumberResult {
  data class Success(val phoneNumber: String) : PhoneNumberResult
  data class Error(val message: String) : PhoneNumberResult
}
