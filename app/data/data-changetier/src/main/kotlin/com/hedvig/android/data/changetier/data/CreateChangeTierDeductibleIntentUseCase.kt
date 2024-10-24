package com.hedvig.android.data.changetier.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productVariant.android.toProductVariant
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.ChangeTierDeductibleCreateIntentMutation
import octopus.fragment.DeductibleFragment

internal interface CreateChangeTierDeductibleIntentUseCase {
  suspend fun invoke(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent>
}

internal class CreateChangeTierDeductibleIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : CreateChangeTierDeductibleIntentUseCase {
  override suspend fun invoke(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    return either {
      val isTierEnabled = featureManager.isFeatureEnabled(Feature.TIER).first()
      if (!isTierEnabled) {
        logcat(ERROR) { "Tried to get changeTierQuotes when feature flag is disabled!" }
        raise(ErrorMessage())
      } else {
        val changeTierDeductibleResponse = apolloClient
          .mutation(
            ChangeTierDeductibleCreateIntentMutation(
              contractId = insuranceId,
              source = source.toSource(),
            ),
          )
          .safeExecute()
        val intent = changeTierDeductibleResponse.getOrNull()?.changeTierDeductibleCreateIntent?.intent
        if (intent != null) {
          val currentQuote = with(intent.agreementToChange) {
            TierDeductibleQuote(
              id = TierConstants.CURRENT_ID,
              deductible = deductible?.toDeductible(),
              premium = UiMoney.fromMoneyFragment(premium),
              productVariant = productVariant.toProductVariant(),
              tier = Tier(
                tierName = tierName ?: "", // todo: what do we do here?
                tierLevel = tierLevel ?: 0, // todo: what do we do here?
                tierDescription = productVariant.tierDescription,
                tierDisplayName = productVariant.displayNameTier,
              ),
              displayItems = displayItems.map {
                ChangeTierDeductibleDisplayItem(
                  displayTitle = it.displayTitle,
                  displaySubtitle = it.displaySubtitle,
                  displayValue = it.displayValue,
                )
              },
            )
          }
          val quotesToOffer = intent.quotes.map {
            ensureNotNull(it.tierLevel) {
              ErrorMessage("For insuranceId:$insuranceId and source:$source, tierLevel was null")
            }
            ensureNotNull(it.tierName) {
              ErrorMessage("For insuranceId:$insuranceId and source:$source, tierName was null")
            }
            TierDeductibleQuote(
              id = it.id,
              deductible = it.deductible?.toDeductible(),
              displayItems = it.displayItems.toDisplayItems(),
              premium = UiMoney.fromMoneyFragment(it.premium),
              productVariant = it.productVariant.toProductVariant(),
              tier = Tier(
                tierName = it.tierName,
                tierLevel = it.tierLevel,
                tierDescription = it.productVariant.tierDescription,
                tierDisplayName = it.productVariant.displayNameTier,
              ),
            )
          }
          ChangeTierDeductibleIntent(
            activationDate = intent.activationDate,
            quotes = listOf(currentQuote) + quotesToOffer,
          )
        } else {
          if (changeTierDeductibleResponse.isRight()) {
            logcat(ERROR) { "Tried to get changeTierQuotes but output intent is null!" }
          }
          if (changeTierDeductibleResponse.isLeft()) {
            logcat(ERROR) { "Tried to get changeTierQuotes but got error: $changeTierDeductibleResponse!" }
          }
          raise(ErrorMessage())
        }
      }
    }
  }
}

private fun DeductibleFragment.toDeductible(): Deductible {
  return Deductible(
    deductibleAmount = UiMoney.fromMoneyFragment(this.amount),
    deductiblePercentage = this.percentage,
    description = this.displayText,
  )
}

private fun List<ChangeTierDeductibleCreateIntentMutation.Data.ChangeTierDeductibleCreateIntent.Intent.Quote.DisplayItem>.toDisplayItems(): List<ChangeTierDeductibleDisplayItem> {
  return this.map {
    ChangeTierDeductibleDisplayItem(
      displayTitle = it.displayTitle,
      displaySubtitle = it.displaySubtitle,
      displayValue = it.displayValue,
    )
  }
}
