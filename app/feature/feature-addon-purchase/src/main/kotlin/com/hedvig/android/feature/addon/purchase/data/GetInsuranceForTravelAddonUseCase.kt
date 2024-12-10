package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
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
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import octopus.InsurancesForTravelAddonQuery

interface GetInsuranceForTravelAddonUseCase {
  suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>>
}

internal class GetInsuranceForTravelAddonUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetInsuranceForTravelAddonUseCase {
  override suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>> {
    return combine(
      featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON),
      apolloClient
        .query(InsurancesForTravelAddonQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeFlow(::ErrorMessage),
    ) { isEnabled, memberResponse ->
      either {
        if (!isEnabled) {
          logcat(LogPriority.ERROR) {
            "Tried to get list of insurances for addon purchase but the addon feature flag id off!"
          }
          raise(ErrorMessage())
        } else {
          // TODO: remove mock!
//          val result = memberResponse.bind().currentMember.toInsurancesForAddon(ids)
//          ensure(result.isNotEmpty()) {
//            { "Tried to get list of insurances for addon purchase but the list is empty!" }
//            ErrorMessage()
//          }
//          result
          listOf<InsuranceForAddon>(
            InsuranceForAddon(
              id = "id",
              displayName = "Rent Bas",
              "Tulegatan 1",
              ContractGroup.RENTAL,
            ),
          ) // TODO: remove mock!!
        }
      }
    }
  }
}

data class InsuranceForAddon(
  val id: String,
  val displayName: String,
  val contractExposure: String,
  val contractGroup: ContractGroup,
)

private fun InsurancesForTravelAddonQuery.Data.CurrentMember.toInsurancesForAddon(
  ids: List<String>,
): List<InsuranceForAddon> {
  return activeContracts
    .filter { ids.contains(it.id) }
    .map {
      InsuranceForAddon(
        id = it.id,
        displayName = it.currentAgreement.productVariant.displayName,
        contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
        contractExposure = it.exposureDisplayName,
      )
    }
}
