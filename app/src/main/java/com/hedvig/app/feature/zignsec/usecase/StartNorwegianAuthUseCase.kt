package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class StartNorwegianAuthUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(nationalIdentityNumber: String) =
        when (val response = apolloClient.mutate(NorwegianBankIdAuthMutation(nationalIdentityNumber)).safeQuery()) {
            is QueryResult.Error -> SimpleSignStartAuthResult.Error
            is QueryResult.Success -> {
                val redirectUrl = response.data.norwegianBankIdAuth.redirectUrl
                SimpleSignStartAuthResult.Success(redirectUrl)
            }
        }
}
