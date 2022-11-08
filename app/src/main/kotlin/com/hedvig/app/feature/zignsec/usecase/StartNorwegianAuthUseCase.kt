package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.NorwegianBankIdAuthMutation
import com.hedvig.android.apollo.safeExecute

class StartNorwegianAuthUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(nationalIdentityNumber: String): SimpleSignStartAuthResult =
    when (val response = apolloClient.mutation(NorwegianBankIdAuthMutation(nationalIdentityNumber)).safeExecute()) {
      is OperationResult.Error -> SimpleSignStartAuthResult.Error
      is OperationResult.Success -> {
        val redirectUrl = response.data.norwegianBankIdAuth.redirectUrl
        SimpleSignStartAuthResult.Success(redirectUrl)
      }
    }
}
