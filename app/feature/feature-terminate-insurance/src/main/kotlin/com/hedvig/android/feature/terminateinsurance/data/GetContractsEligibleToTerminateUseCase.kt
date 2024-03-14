package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate

internal interface GetContractsEligibleToTerminateUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceEligibleForTermination>>
}

internal class GetContractsEligibleToTerminateUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetContractsEligibleToTerminateUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceEligibleForTermination>> {
    TODO("Not yet implemented")
  }
}

data class InsuranceEligibleForTermination(
  val id: String,
  val displayName: String,
  val displayDetails: String,
  val monthlyPayment: UiMoney,
  val nextPaymentDate: LocalDate?,
)
