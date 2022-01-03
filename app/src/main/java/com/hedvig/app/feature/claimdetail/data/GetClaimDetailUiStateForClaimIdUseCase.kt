package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.flatMap
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetClaimDetailUiStateForClaimIdUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    sealed interface Error {
        object NetworkError : Error
        object NoClaimFound : Error
    }

    suspend operator fun invoke(claimId: String): Either<Error, ClaimDetailUiState> {
        return apolloClient
            .query(ClaimDetailsQuery(localeManager.defaultLocale()))
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
            .map(ClaimDetailUiState::fromDto)
    }
}
