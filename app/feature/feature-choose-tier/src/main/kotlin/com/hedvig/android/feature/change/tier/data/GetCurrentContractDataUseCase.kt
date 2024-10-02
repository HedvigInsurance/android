package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
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
    // todo: remove mock!!!

    return either {
      CurrentContractData(
        currentExposureName = "Testsgatan 555",
        currentDisplayPremium = UiMoney(295.0, SEK),
        deductible = Deductible(
          UiMoney(1000.0, SEK),
          deductiblePercentage = 25,
          description = "En fast del och en rörlig del om 25% av skadekostnaden.",
        ),
        productVariant = ProductVariant(
          displayName = "Test",
          contractGroup = ContractGroup.RENTAL,
          contractType = ContractType.SE_APARTMENT_RENT,
          partner = "test",
          perils = listOf(),
          insurableLimits = listOf(),
          documents = listOf(),
        ),
      )
    }
  }
}

data class CurrentContractData(
  val currentExposureName: String,
  val currentDisplayPremium: UiMoney,
  val deductible: Deductible,
  val productVariant: ProductVariant,
)
