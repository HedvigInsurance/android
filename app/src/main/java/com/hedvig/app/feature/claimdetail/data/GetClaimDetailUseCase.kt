package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.flatMap
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetClaimDetailUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    sealed interface Error {
        object NetworkError : Error
        object NoClaimFound : Error
    }

    private val queryCall: ApolloQueryCall<ClaimDetailsQuery.Data>
        get() = apolloClient
            .query(ClaimDetailsQuery(localeManager.defaultLocale()))
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()

    suspend operator fun invoke(claimId: String): Either<Error, ClaimDetailsQuery.ClaimDetail> {
        return queryCall
            .safeQuery()
            .toEither {
                Error.NetworkError
            }
            .flatMap { data ->
                data.claimDetails
                    .firstOrNone { it.id == claimId }
                    .toEither {
                        Error.NoClaimFound
                    }
            }
    }
}
