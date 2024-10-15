package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
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
        raise(ErrorMessage("Tried to start Change Tier flow when feature flag is disabled"))
      }
      val result = apolloClient
        .query(CurrentContractsForTierChangeQuery())
        .safeExecute()
        .mapLeft { ErrorMessage("Tried to start Change Tier flow but got error from CurrentContractsQuery") }
        .onLeft {
          logcat(ERROR) { "Tried to start Change Tier flow but got error from CurrentContractsQuery" }
        }
        .bind()
      val dataResult = result.currentMember.activeContracts.firstOrNull { it.id == insuranceId }
      if (dataResult == null) {
        logcat(ERROR) { "Tried to start Change Tier flow but got null active contract" }
        raise(ErrorMessage("Tried to start Change Tier flow but got null active contract"))
      } else {
        if (dataResult.upcomingChangedAgreement!=null) {
          val deductible = dataResult.upcomingChangedAgreement.deductible?.let {
            Deductible(
              deductibleAmount = UiMoney.fromMoneyFragment(it.amount),
              deductiblePercentage = it.percentage,
              description = it.displayText,
            )
          }
          CurrentContractData(
            currentExposureName = dataResult.exposureDisplayName,
            currentDisplayPremium = UiMoney.fromMoneyFragment(dataResult.upcomingChangedAgreement.premium),
            deductible = deductible,
            productVariant = dataResult.upcomingChangedAgreement.productVariant.toProductVariant(),
            displayItems = dataResult.upcomingChangedAgreement.displayItems.map {
              ChangeTierDeductibleDisplayItem(
                displayTitle = it.displayTitle,
                displaySubtitle = it.displaySubtitle,
                displayValue = it.displayValue,
              )
            }
          )
        } else {
          val deductible = dataResult.currentAgreement.deductible?.let {
            Deductible(
              deductibleAmount = UiMoney.fromMoneyFragment(it.amount),
              deductiblePercentage = it.percentage,
              description = it.displayText,
            )
          }
          CurrentContractData(
            currentExposureName = dataResult.exposureDisplayName,
            currentDisplayPremium = UiMoney.fromMoneyFragment(dataResult.currentAgreement.premium),
            deductible = deductible,
            productVariant = dataResult.currentAgreement.productVariant.toProductVariant(),
            displayItems = dataResult.currentAgreement.displayItems.map {
              ChangeTierDeductibleDisplayItem(
                displayTitle = it.displayTitle,
                displaySubtitle = it.displaySubtitle,
                displayValue = it.displayValue,
              )
            }
          )
        }
      }
    }
  }
}

data class CurrentContractData(
  val currentExposureName: String,
  val currentDisplayPremium: UiMoney,
  val deductible: Deductible?,
  val productVariant: ProductVariant,
  val displayItems: List<ChangeTierDeductibleDisplayItem>
)
