package com.hedvig.android.data.termination.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
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
  suspend fun invoke(): Flow<Either<ErrorMessage, List<TerminatableInsurance>?>>
}

internal class GetTerminatableContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTerminatableContractsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<TerminatableInsurance>?>> {
    return combine(
      featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
      apolloClient
        .query(ContractsToTerminateQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeFlow(::ErrorMessage),
    ) { isEnabled, memberResponse ->
      either {
        val memberData = memberResponse.bind().currentMember
        val uncheckedContracts = memberData.toInsurancesForCancellation()
        val checkedContracts = uncheckedContracts.takeIf { isEnabled && uncheckedContracts.isNotEmpty() }
        checkedContracts
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
