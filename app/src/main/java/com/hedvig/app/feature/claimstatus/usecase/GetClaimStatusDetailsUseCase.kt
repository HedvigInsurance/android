package com.hedvig.app.feature.claimstatus.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ClaimStatusDetailsQuery
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.map
import com.hedvig.app.util.apollo.safeQuery
import e

class GetClaimStatusDetailsUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    sealed class ClaimStatusDetailResult {
        object Error : ClaimStatusDetailResult()
        data class Success(
            val data: ClaimStatusDetailData,
        ) : ClaimStatusDetailResult()
    }

    suspend operator fun invoke(
        claimId: String,
    ): ClaimStatusDetailResult {
        val result = apolloClient
            .query(ClaimStatusDetailsQuery(localeManager.defaultLocale()))
            .safeQuery()
            .map { ClaimStatusDetailData.fromQueryModel(it, claimId) }
        return when (result) {
            is QueryResult.Success -> {
                ClaimStatusDetailResult.Success(result.data)
            }
            is QueryResult.Error -> {
                e { "Error when loading claim status details: ${result.message}" }
                ClaimStatusDetailResult.Error
            }
        }
    }
}
