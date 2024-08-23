package com.hedvig.android.data.termination.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import octopus.ContractsToTerminateQuery

interface GetTerminatableContractsUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<TerminatableInsurance>?>>
}

internal class GetTerminatableContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTerminatableContractsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, NonEmptyList<TerminatableInsurance>?>> {
    return combine(
      featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
      apolloClient
        .query(ContractsToTerminateQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeFlow(::ErrorMessage),
    ) { isEnabled, memberResponse ->
      either {
        if (!isEnabled) return@either null
        memberResponse.bind().currentMember.toInsurancesForCancellation().toNonEmptyListOrNull()
      }
    }
  }
}

data class TerminatableInsurance(
  val id: String,
  val displayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
  val activateFrom: LocalDate,
)

private fun ContractsToTerminateQuery.Data.CurrentMember.toInsurancesForCancellation(): List<TerminatableInsurance> {
  return activeContracts
    .filter {
      it.terminationDate == null
    }
    .map {
      TerminatableInsurance(
        id = it.id,
        displayName = it.currentAgreement.productVariant.displayName,
        contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
        contractExposure = it.exposureDisplayName,
        activateFrom = it.currentAgreement.activeFrom,
      )
    }
}
