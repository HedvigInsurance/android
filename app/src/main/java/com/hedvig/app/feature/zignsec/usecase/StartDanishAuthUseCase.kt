package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class StartDanishAuthUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(personalIdentificationNumber: String) =
        when (val response = apolloClient.mutate(DanishAuthMutation(personalIdentificationNumber)).safeQuery()) {
            is QueryResult.Error -> SimpleSignStartAuthResult.Error
            is QueryResult.Success -> {
                val redirectUrl = response.data.danishBankIdAuth.redirectUrl
                SimpleSignStartAuthResult.Success(redirectUrl)
            }
        }
}
