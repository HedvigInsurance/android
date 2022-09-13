package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.DanishAuthMutation
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute

class StartDanishAuthUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(personalIdentificationNumber: String): SimpleSignStartAuthResult =
    when (val response = apolloClient.mutation(DanishAuthMutation(personalIdentificationNumber)).safeExecute()) {
      is OperationResult.Error -> SimpleSignStartAuthResult.Error
      is OperationResult.Success -> {
        val redirectUrl = response.data.danishBankIdAuth.redirectUrl
        SimpleSignStartAuthResult.Success(redirectUrl)
      }
    }
}
