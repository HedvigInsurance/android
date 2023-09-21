package com.hedvig.android.feature.insurances.insurancedetail.coverage

import androidx.compose.runtime.saveable.listSaver
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import octopus.ContractCoverageQuery

internal interface GetContractCoverageUseCase {
  suspend fun invoke(contractId: String): Either<ErrorMessage, ContractCoverage>
}

internal class GetContractCoverageUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetContractCoverageUseCase {
  override suspend fun invoke(
    contractId: String,
  ): Either<ErrorMessage, ContractCoverage> {
    return either {
      val data = apolloClient
        .query(ContractCoverageQuery(contractId))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      val contract = data.contract
      ensureNotNull(contract) { ErrorMessage("Contract not found") }
      ContractCoverage.fromContract(contract)
    }
  }
}

internal data class ContractCoverage(
  val contractPerils: ImmutableList<Peril>,
  val insurableLimits: ImmutableList<InsurableLimit>,
) {
  internal data class InsurableLimit(
    val label: String,
    val limit: String,
    val description: String,
  ) {
    companion object {
      val Saver = listSaver<InsurableLimit?, String>(
        save = {
          if (it == null) {
            emptyList()
          } else {
            listOf<String>(
              it.label,
              it.limit,
              it.description,
            )
          }
        },
        restore = {
          if (it.size != 3) {
            null
          } else {
            InsurableLimit(
              label = it[0],
              limit = it[1],
              description = it[2],
            )
          }
        },
      )
    }
  }

  internal data class Peril(
    val id: String,
    val title: String,
    val description: String,
    val covered: ImmutableList<String>,
    val colorHexValue: Long?,
  )

  companion object {
    fun fromContract(contract: ContractCoverageQuery.Data.Contract): ContractCoverage {
      return ContractCoverage(
        contractPerils = contract.variant.perils
          .map { peril ->
            Peril(
              id = peril.id,
              title = peril.title,
              description = peril.description,
              covered = peril.covered.toPersistentList(),
              colorHexValue = peril
                .colorCode
                ?.trim('#')
                ?.takeIf { it.length == 6 }
                ?.let { "FF$it" }
                ?.toLongOrNull(16),
            )
          }
          .toPersistentList(),
        insurableLimits = contract.variant.insurableLimits
          .map { insurableLimit ->
            InsurableLimit(
              label = insurableLimit.label,
              limit = insurableLimit.limit,
              description = insurableLimit.description.trim(),
            )
          }
          .toPersistentList(),
      )
    }
  }
}
