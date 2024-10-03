package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.productVariant.android.toProductVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature.TIER
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.CurrentContractsForTierChangeQuery

internal interface GetCurrentContractDataUseCase {
  suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData>
}

internal class GetCurrentContractDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either {
      val isTierEnabled = featureManager.isFeatureEnabled(TIER).first()
      if (!isTierEnabled) {
        logcat(ERROR) { "Tried to start Change Tier flow when feature flag is disabled" }
        raise(ErrorMessage())
      } else {
        val result = apolloClient.query(CurrentContractsForTierChangeQuery()).safeExecute().getOrNull()
        if (result == null) {
          logcat(ERROR) { "Tried to start Change Tier flow but got error from CurrentContractsQuery" }
          raise(ErrorMessage())
        } else {
          val dataResult = result.currentMember.activeContracts.firstOrNull { it.id == insuranceId }
          if (dataResult == null) {
            logcat(ERROR) { "Tried to start Change Tier flow but got null active contract" }
            raise(ErrorMessage())
          } else {
            val deductible = Deductible(
              deductibleAmount = dataResult.currentAgreement.deductible?.amount?.let {
                UiMoney.fromMoneyFragment(it)
              },
              deductiblePercentage = dataResult.currentAgreement.deductible?.percentage,
              description = dataResult.currentAgreement.deductible?.displayText ?: "",
            )
            logcat { "Mariiaaa: current deductible: $deductible" }
            CurrentContractData(
              currentExposureName = dataResult.exposureDisplayName,
              currentDisplayPremium = UiMoney.fromMoneyFragment(dataResult.currentAgreement.premium),
              deductible = deductible,
              productVariant = dataResult.currentAgreement.productVariant.toProductVariant(),
            )
          }
        }
      }
//    // todo: remove mock!!!
//      CurrentContractData(
//        currentExposureName = "Testsgatan 555",
//        currentDisplayPremium = UiMoney(295.0, SEK),
//        deductible = Deductible(
//          UiMoney(1000.0, SEK),
//          deductiblePercentage = 25,
//          description = "En fast del och en rörlig del om 25% av skadekostnaden.",
//        ),
//        productVariant = ProductVariant(
//          displayName = "Test",
//          contractGroup = ContractGroup.RENTAL,
//          contractType = ContractType.SE_APARTMENT_RENT,
//          partner = "test",
//          perils = listOf(),
//          insurableLimits = listOf(),
//          documents = listOf(),
//        ),
//      )
    }
  }
}

data class CurrentContractData(
  val currentExposureName: String,
  val currentDisplayPremium: UiMoney,
  val deductible: Deductible,
  val productVariant: ProductVariant,
)
