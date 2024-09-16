package com.hedvig.android.data.paying.member

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.toContractType
import octopus.ActiveInsuranceContractTypesQuery

internal class GetOnlyHasNonPayingContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetOnlyHasNonPayingContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Boolean> {
    return either {
      val contractTypes: List<ContractType> =
        apolloClient.query(ActiveInsuranceContractTypesQuery())
          .safeExecute(::ErrorMessage)
          .bind()
          .currentMember
          .activeContracts
          .map { it.currentAgreement.productVariant.typeOfContract.toContractType() }

      contractTypes.all { it.isNonPayingContractType() }
    }
  }
}

private fun ContractType.isNonPayingContractType(): Boolean {
  return this == ContractType.SE_QASA_SHORT_TERM_RENTAL || this == ContractType.SE_QASA_LONG_TERM_RENTAL
}
