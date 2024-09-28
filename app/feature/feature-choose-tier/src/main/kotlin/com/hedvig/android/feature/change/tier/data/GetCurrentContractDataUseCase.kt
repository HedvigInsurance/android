package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup

interface GetCurrentContractDataUseCase {
  suspend fun invoke( insuranceId: String): Either<ErrorMessage, CurrentContractData>
}

internal class GetCurrentContractDataUseCaseImpl(
  apolloClient: ApolloClient
): GetCurrentContractDataUseCase {
  override suspend fun invoke( insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    TODO("Not yet implemented")
  }
}

data class CurrentContractData(
  val contractGroup: ContractGroup,
  val displayName: String,
  val displaySubtitle: String,
  val currentDisplayPremium: String,
  val tier: Tier,
  val deductible: Deductible,
)
