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
)

private fun ContractsToTerminateQuery.Data.CurrentMember.toInsurancesForCancellation(): List<TerminatableInsurance> {
  val active = activeContracts
    .filter {
      it.terminationDate == null
    }
    .map {
      TerminatableInsurance(
        id = it.id,
        displayName = it.currentAgreement.productVariant.displayName,
        contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
        contractExposure = it.exposureDisplayName,
      )
    }
  val pending = pendingContracts.map {
    TerminatableInsurance(
      id = it.id,
      displayName = it.productVariant.displayName,
      contractGroup = it.productVariant.typeOfContract.toContractGroup(),
      contractExposure = it.exposureDisplayName,
    )
  }
  return active + pending
}
