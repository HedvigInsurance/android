package com.hedvig.app.feature.insurance.ui.detail

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.flatMap
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager

class GetContractDetailsUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    featureManager: FeatureManager,
) {

    private val isMovingFlowEnabled = featureManager.isFeatureEnabled(Feature.MOVING_FLOW)

    suspend operator fun invoke(contractId: String): Either<ContractDetailError, ContractDetailViewState> {
        return apolloClient
            .query(InsuranceQuery(localeManager.defaultLocale()))
            .safeQuery()
            .toEither { ContractDetailError.NetworkError }
            .flatMap { data ->
                data.contracts
                    .firstOrNone { it.id == contractId }
                    .toEither { ContractDetailError.ContractNotFoundError }
                    .map { it.toContractDetailViewState(isMovingFlowEnabled) }
            }
    }

    sealed class ContractDetailError {
        object NetworkError : ContractDetailError()
        object ContractNotFoundError : ContractDetailError()
    }
}
