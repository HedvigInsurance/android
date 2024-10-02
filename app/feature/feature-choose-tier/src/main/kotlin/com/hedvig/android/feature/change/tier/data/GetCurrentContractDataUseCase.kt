package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.featureflags.FeatureManager

internal interface GetCurrentContractDataUseCase {
  suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData>
}

internal class GetCurrentContractDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    TODO("Not yet implemented")
  }
}

data class CurrentContractData(
  val currentExposureName: String,
  val currentDisplayPremium: UiMoney,
  val deductible: Deductible,
  val productVariant: ProductVariant,
)
