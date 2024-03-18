package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import kotlinx.datetime.LocalDate
import octopus.ContractsToTerminateQuery

internal interface GetContractsToTerminateUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceForCancellation>>
}

internal class GetContractsToTerminateUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetContractsToTerminateUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceForCancellation>> {
    return apolloClient
      .query(ContractsToTerminateQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { data ->
        val futureCharge = data.currentMember.futureCharge
        if (futureCharge != null) {
          val list = data.currentMember.futureCharge?.contractsChargeBreakdown ?: listOf()
          val mappedList = list.map { breakdown ->
            breakdown.toInsuranceForCancellation(futureCharge.date)
          }
          mappedList
        } else {
          listOf()
        }
      }
  }
}

data class InsuranceForCancellation(
  val id: String,
  val displayName: String,
  val monthlyPayment: UiMoney,
  val nextPaymentDate: LocalDate?,
  val contractExposure: String,
  val contractGroup: ContractGroup,
  val activateFrom: LocalDate,
)

private fun ContractsToTerminateQuery.Data.CurrentMember.FutureCharge
  .ContractsChargeBreakdown.toInsuranceForCancellation(
  date: LocalDate,
): InsuranceForCancellation {
  return InsuranceForCancellation(
    id = contract.id,
    displayName = contract.currentAgreement.productVariant.displayName,
    contractGroup = contract.currentAgreement.productVariant.typeOfContract.toContractGroup(),
    contractExposure = contract.exposureDisplayName,
    activateFrom = contract.currentAgreement.activeFrom,
    monthlyPayment = UiMoney.fromMoneyFragment(gross),
    nextPaymentDate = date,
  )
}
