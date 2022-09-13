package com.hedvig.app.feature.insurance.ui.detail

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.flatMap
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.util.LocaleManager

class GetContractDetailsUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {

  suspend operator fun invoke(contractId: String): Either<ContractDetailError, ContractDetailViewState> {
    return apolloClient
      .query(InsuranceQuery(localeManager.defaultLocale()))
      .safeExecute()
      .toEither { ContractDetailError.NetworkError }
      .flatMap { data ->
        data.contracts
          .firstOrNone { it.id == contractId }
          .toEither { ContractDetailError.ContractNotFoundError }
          .map { it.toContractDetailViewState() }
      }
  }

  sealed class ContractDetailError {
    object NetworkError : ContractDetailError()
    object ContractNotFoundError : ContractDetailError()
  }
}
