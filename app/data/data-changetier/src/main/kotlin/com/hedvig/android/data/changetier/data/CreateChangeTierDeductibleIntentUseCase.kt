package com.hedvig.android.data.changetier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productVariant.android.toProductVariant
import com.hedvig.android.data.productvariant.ProductVariant
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
//        ChangeTierDeductibleIntent(
//          activationDate = LocalDate(2024, 10, 3),
//          currentTierName = "Standard",
//          currentTierLevel = 1,
//          quotes = quotesForPreview,
//        )
        // todo: remove mock!!!

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
                  tierDescription = it.productVariant.tierDescription,
                  tierDisplayName = it.productVariant.displayNameTier,
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

private val quotesForPreview = listOf(
  TierDeductibleQuote(
    id = "id0",
    deductible = Deductible(
      UiMoney(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(199.0, SEK),
    tier = Tier("BAS", tierLevel = 0, tierDescription = "Vårt paket med grundläggande villkor.", tierDisplayName = "Bas"),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
    ),
  ),
  TierDeductibleQuote(
    id = "id1",
    deductible = Deductible(
      UiMoney(1000.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(255.0, SEK),
    tier = Tier("BAS", tierLevel = 0, tierDescription = "Vårt paket med grundläggande villkor.", tierDisplayName = "Bas"),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
    ),
  ),
  TierDeductibleQuote(
    id = "id2",
    deductible = Deductible(
      UiMoney(3500.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
    displayItems = listOf(),
    premium = UiMoney(355.0, SEK),
    tier = Tier("BAS", tierLevel = 0, tierDescription = "Vårt paket med grundläggande villkor.", tierDisplayName = "Bas"),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
    ),
  ),
  TierDeductibleQuote(
    id = "id3",
    deductible = Deductible(
      UiMoney(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(),
    premium = UiMoney(230.0, SEK),
    tier = Tier("STANDARD", tierLevel = 1, tierDescription = "Vårt mellanpaket med hög ersättning.", tierDisplayName = "Standard"),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
    ),
  ),
  TierDeductibleQuote(
    id = "id4",
    deductible = Deductible(
      UiMoney(3500.0, SEK),
      deductiblePercentage = 25,
      description = "En fast del och en rörlig del om 25% av skadekostnaden",
    ),
    displayItems = listOf(),
    premium = UiMoney(655.0, SEK),
    tier = Tier("STANDARD", tierLevel = 1, tierDescription = "Vårt mellanpaket med hög ersättning.", tierDisplayName = "Standard"),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
    ),
  ),
)
