package com.hedvig.app.feature.insurance.ui.detail

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceQuery

class GetContractDetailsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val featureManager: FeatureManager,
) {

  suspend fun invoke(contractId: String): Either<ContractDetailError, ContractDetailViewState> {
    return either {
      val data = apolloClient
        .query(InsuranceQuery(languageService.getGraphQLLocale()))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither { ContractDetailError.NetworkError }
        .bind()
      val contract = data.contracts.firstOrNull { it.id == contractId }
      ensureNotNull(contract) { ContractDetailError.ContractNotFoundError }
      contract.toContractDetailViewState(featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW))
    }
  }

  sealed class ContractDetailError {
    object NetworkError : ContractDetailError()
    object ContractNotFoundError : ContractDetailError()
  }
}
