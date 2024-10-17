package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.AvailableSelfServiceOnContractsQuery

internal interface GetInsuranceForEditCoInsuredUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>
}

internal class GetInsuranceForEditCoInsuredUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) :
  GetInsuranceForEditCoInsuredUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>> {
    return either {
      val contracts = apolloClient.query(AvailableSelfServiceOnContractsQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
        .bind()
        .currentMember
        .activeContracts
      val filtered = contracts
        .filter { it.supportsCoInsured }
        .takeIf { featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).first() }
      buildList {
        filtered?.forEach { contract ->
          val destination = if (contract.coInsured?.any { it.hasMissingInfo } == true) {
            QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo(contract.id)
          } else {
            QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove(contract.id)
          }
          add(
            InsuranceForEditOrAddCoInsured(
              quickLinkDestination = destination,
              displayName = contract.currentAgreement.productVariant.displayName,
              exposureName = contract.exposureDisplayName,
              id = contract.id,
            ),
          )
        }
      }
    }
  }
}

internal data class InsuranceForEditOrAddCoInsured(
  val id: String,
  val quickLinkDestination: QuickLinkDestination.OuterDestination,
  val displayName: String,
  val exposureName: String,
)
