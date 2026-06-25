package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.InsurancesForTravelAddonQuery

internal interface GetInsuranceForTravelAddonUseCase {
  suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInsuranceForTravelAddonUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInsuranceForTravelAddonUseCase {
  override suspend fun invoke(ids: List<String>): Flow<Either<ErrorMessage, List<InsuranceForAddon>>> {
    return apolloClient
      .query(InsurancesForTravelAddonQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow(::ErrorMessage)
      .map { memberResponse ->
        either {
          val result = memberResponse.bind().currentMember.toInsurancesForAddon(ids)
          ensure(result.isNotEmpty()) {
            logcat { "Tried to get list of insurances for addon purchase but the list is empty!" }
            ErrorMessage()
          }
          result
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
        contractExposure = it.exposureDisplayNameShort,
      )
    }
}
