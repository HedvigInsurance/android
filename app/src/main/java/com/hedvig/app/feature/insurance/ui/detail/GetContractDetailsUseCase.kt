package com.hedvig.app.feature.insurance.ui.detail

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.firstOrNone
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class GetContractDetailsUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend operator fun invoke(contractId: String): Either<ContractDetailError, ContractDetailViewState> {
        return either {
            val insuranceQueryData = apolloClient
                .query(InsuranceQuery(localeManager.defaultLocale()))
                .safeQuery()
                .toEither { ContractDetailError.NetworkError }
                .bind()
            val contract = insuranceQueryData
                .contracts
                .firstOrNone { it.id == contractId }
                .toEither { ContractDetailError.ContractNotFoundError }
                .bind()
            contract.toContractDetailViewState()
        }
    }

    sealed class ContractDetailError {
        object NetworkError : ContractDetailError()
        object ContractNotFoundError : ContractDetailError()
    }
}
