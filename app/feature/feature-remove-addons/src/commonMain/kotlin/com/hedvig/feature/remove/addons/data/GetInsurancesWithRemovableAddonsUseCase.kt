package com.hedvig.feature.remove.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.logger.logcat
import octopus.InsurancesWithRemovableAddonsQuery

interface GetInsurancesWithRemovableAddonsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceForAddon>>
}

internal class GetInsurancesWithRemovableAddonsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInsurancesWithRemovableAddonsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceForAddon>> {
    return either {
      val memberResponse = apolloClient
        .query(InsurancesWithRemovableAddonsQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
      memberResponse.fold(
        ifLeft = {
          logcat { "InsurancesWithRemovableAddonsQuery returned error: $it" }
          raise(ErrorMessage())
        },
        ifRight = {
          //TODO: filter out non-removable addons!
          it.currentMember.activeContracts.toInsurancesForAddon()
        }
      )
    }
  }
}


data class InsuranceForAddon(
  val id: String,
  val displayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
)

private fun List<InsurancesWithRemovableAddonsQuery.Data.CurrentMember.ActiveContract>.toInsurancesForAddon():
  List<InsuranceForAddon> {
  return filter {it.currentAgreement.addons.isNotEmpty()} //todo: redo, change for it.existingAddons
    .map { contract ->
    InsuranceForAddon(
      id = contract.id,
      displayName = contract.currentAgreement.productVariant.displayName,
      contractExposure = contract.exposureDisplayName,
      contractGroup = contract.currentAgreement.productVariant.typeOfContract.toContractGroup(),
    )
  }
}
