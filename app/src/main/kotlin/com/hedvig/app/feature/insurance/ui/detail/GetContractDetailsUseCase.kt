package com.hedvig.app.feature.insurance.ui.detail

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.apollo.toEither
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetContractDetailsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val featureManager: FeatureManager,
) {
  fun invoke(contractId: String): Flow<Either<ContractDetailError, ContractDetailViewState>> {
    return apolloClient
      .query(InsuranceQuery(languageService.getGraphQLLocale()))
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow()
      .map { it.toEither { ContractDetailError.NetworkError } }
      .map { result ->
        either {
          val data = result.bind()
          val contract = data.contracts.firstOrNull { it.id == contractId }
          ensureNotNull(contract) { ContractDetailError.ContractNotFoundError }
          contract.toContractDetailViewState(featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW))
        }
      }
  }

  sealed class ContractDetailError {
    object NetworkError : ContractDetailError()
    object ContractNotFoundError : ContractDetailError()
  }
}
