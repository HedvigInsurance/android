package com.hedvig.app.feature.swedishbankid.sign.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.SignStatusQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e

class ManuallyRecheckSwedishBankIdSignStatusUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(): SignStatusFragment? {
        val result = apolloClient
            .query(SignStatusQuery())
            .safeQuery()

        return when (result) {
            is QueryResult.Error -> {
                e { "Error manually rechecking sign status: ${result.message}" }
                null
            }
            is QueryResult.Success -> result.data.signStatus?.fragments?.signStatusFragment
        }
    }
}
