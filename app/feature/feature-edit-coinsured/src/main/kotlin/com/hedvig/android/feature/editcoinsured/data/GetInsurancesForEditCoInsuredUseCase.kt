package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.EligibleContractsForEditCoInsuredQuery

internal interface GetInsurancesForEditCoInsuredUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>
}

internal class GetInsurancesForEditCoInsuredUseCaseImpl(
  private val apolloClient: ApolloClient,
) :
  GetInsurancesForEditCoInsuredUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>> {
    return either {
      val contracts = apolloClient.query(EligibleContractsForEditCoInsuredQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
        .bind()
        .currentMember
        .activeContracts
      val filtered = contracts
        .filter { it.supportsCoInsured }
      buildList {
        filtered.forEach { contract ->
          val destination = if (contract.coInsured?.any { it.hasMissingInfo } == true) {
            EditCoInsuredDestination.MISSING_INFO
          } else {
            EditCoInsuredDestination.ADD_OR_REMOVE
          }
          add(
            InsuranceForEditOrAddCoInsured(
              destination = destination,
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
  val destination: EditCoInsuredDestination,
  val displayName: String,
  val exposureName: String,
)

internal enum class EditCoInsuredDestination {
  MISSING_INFO,
  ADD_OR_REMOVE
}
