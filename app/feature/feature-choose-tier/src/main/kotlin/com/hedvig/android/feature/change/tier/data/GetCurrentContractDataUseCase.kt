package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant


interface GetCurrentContractDataUseCase {
  suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData>
}

internal class GetCurrentContractDataUseCaseImpl(
  val apolloClient: ApolloClient,
) : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    TODO("Not yet implemented")
  }
}

data class CurrentContractData(
  val currentExposureName: String,
  val currentDisplayPremium: UiMoney,
  val tier: Tier,
  val deductible: Deductible,
  val productVariant: ProductVariant,
)
