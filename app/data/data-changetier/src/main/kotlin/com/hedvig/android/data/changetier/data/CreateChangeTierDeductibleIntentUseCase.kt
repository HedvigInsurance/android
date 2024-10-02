package com.hedvig.android.data.changetier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
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
          .fetchPolicy(FetchPolicy.NetworkOnly)
          .safeExecute()
        val intent = changeTierDeductibleResponse.getOrNull()?.changeTierDeductibleCreateIntent?.intent
        if (intent != null) {
          try {
            val quotes = intent.quotes.map {
              TierDeductibleQuote(
                id = it.id,
                deductible = it.deductible.toDeductible(),
                displayItems = it.displayItems.toDisplayItems(),
                premium = UiMoney.fromMoneyFragment(it.premium),
                productVariant = it.productVariant.toProductVariant(),
                tier = Tier(
                  tierName = it.tierName!!,
                  tierLevel = it.tierLevel!!,
                  info = it.productVariant.displayNameTierLong,
                ),
              )
            }
            ChangeTierDeductibleIntent(
              activationDate = intent.activationDate,
              currentTierLevel = intent.currentTierLevel,
              currentTierName = intent.currentTierName,
              quotes = quotes,
            )
          } catch (e: Exception) {
            logcat(ERROR) { "Tried to get changeTierQuotes but quotes have tierLevel or tierName == null!" }
            raise(ErrorMessage())
          }
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

private fun ChangeTierDeductibleCreateIntentMutation.Data.ChangeTierDeductibleCreateIntent.Intent.Quote.Deductible?.toDeductible(): Deductible? {
  return if (this != null) {
    Deductible(
      deductibleAmount = UiMoney.fromMoneyFragment(this.amount),
      deductiblePercentage = this.percentage,
      description = this.displayText,
    )
  } else {
    null
  }
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
