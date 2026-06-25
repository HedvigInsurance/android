package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.AvailableSelfServiceOnContractsQuery

internal interface GetInsuranceForEditCoInsuredUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInsuranceForEditCoInsuredUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInsuranceForEditCoInsuredUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>> {
    return either {
      val contracts = apolloClient.query(AvailableSelfServiceOnContractsQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
        .bind()
        .currentMember
        .activeContracts
      buildList {
        contracts.filter { it.supportsCoInsured }.forEach { contract ->
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
        contracts.filter { it.supportsCoOwners }.forEach { contract ->
          val destination = if (contract.coOwners?.any { it.hasMissingInfo } == true) {
            QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo(contract.id)
          } else {
            QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove(contract.id)
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
