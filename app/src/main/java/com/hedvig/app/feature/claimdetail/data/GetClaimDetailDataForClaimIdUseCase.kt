package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.app.feature.claimdetail.model.ClaimDetailData
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.compose.preview.previewData

class GetClaimDetailDataForClaimIdUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend operator fun invoke(claimId: String): Either<QueryResult.Error, ClaimDetailData> {
        val result = apolloClient
            .query(ClaimDetailsQuery(localeManager.defaultLocale()))
            .safeQuery()

        return when (result) {
            is QueryResult.Error -> Either.Left(result)
            is QueryResult.Success -> Either.Right(/*todo result.map()*/ClaimDetailData.previewData().first())
        }
    }
}
