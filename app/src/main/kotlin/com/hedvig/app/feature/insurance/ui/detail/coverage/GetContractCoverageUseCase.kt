package com.hedvig.app.feature.insurance.ui.detail.coverage

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.ContractCoverageQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.util.ErrorMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class GetContractCoverageUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(
    contractId: String,
  ): Either<ErrorMessage, ContractCoverage> {
    return either {
      val data = apolloClient
        .query(ContractCoverageQuery(languageService.getGraphQLLocale()))
        .safeExecute()
        .toEither { ErrorMessage(it) }
        .bind()
      val contract = data.contracts.firstOrNull { it.id == contractId }
      ensureNotNull(contract) { ErrorMessage("Contract not found") }
      ContractCoverage.fromContract(contract)
    }
  }
}

internal data class ContractCoverage(
  val contractDisplayName: String,
  val contractPerils: ImmutableList<Peril>,
  val insurableLimits: ImmutableList<InsurableLimit>,
) {
  internal data class InsurableLimit(
    val label: String,
    val limit: String,
    val description: String,
  )

  companion object {
    fun fromContract(contract: ContractCoverageQuery.Contract): ContractCoverage {
      return ContractCoverage(
        contractDisplayName = contract.displayName,
        contractPerils = contract.contractPerils
          .map { contractPeril ->
            Peril.from(contractPeril.fragments.perilFragment)
          }
          .toPersistentList(),
        insurableLimits = contract.insurableLimits
          .map { it.fragments.insurableLimitsFragment }
          .map { insurableLimit ->
            InsurableLimit(
              label = insurableLimit.label,
              limit = insurableLimit.limit,
              description = insurableLimit.description,
            )
          }
          .toPersistentList(),
      )
    }
  }
}
