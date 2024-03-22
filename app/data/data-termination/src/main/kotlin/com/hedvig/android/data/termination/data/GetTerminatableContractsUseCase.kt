package com.hedvig.android.data.termination.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import kotlinx.datetime.LocalDate
import octopus.ContractsToTerminateQuery


interface GetTerminatableContractsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<TerminatableInsurance>>
}

internal class GetTerminatableContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetTerminatableContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<TerminatableInsurance>> {
    return either {
      val member = apolloClient
        .query(ContractsToTerminateQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .currentMember
      member.toInsurancesForCancellation()
    }
  }
}

data class TerminatableInsurance(
  val id: String,
  val displayName: String,
  val monthlyPayment: UiMoney,
  val nextPaymentDate: LocalDate?,
  val contractExposure: String,
  val contractGroup: ContractGroup,
  val activateFrom: LocalDate,
)

private fun ContractsToTerminateQuery.Data.CurrentMember.toInsurancesForCancellation(): List<TerminatableInsurance> {
  val futureChargeIds = futureCharge?.contractsChargeBreakdown?.map {
    it.contract.id
  } ?: listOf()
  val nextPaymentDate = futureCharge?.date
  return activeContracts.map {
    TerminatableInsurance(
      id = it.id,
      displayName = it.currentAgreement.productVariant.displayName,
      contractGroup = it.currentAgreement.productVariant.typeOfContract.toContractGroup(),
      contractExposure = it.exposureDisplayName,
      activateFrom = it.currentAgreement.activeFrom,
      monthlyPayment = UiMoney.fromMoneyFragment(it.currentAgreement.premium),
      nextPaymentDate = if (it.id in futureChargeIds) nextPaymentDate else null,
    )
  }
}
