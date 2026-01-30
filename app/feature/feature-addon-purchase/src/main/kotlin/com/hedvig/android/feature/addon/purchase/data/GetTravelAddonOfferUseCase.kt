package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.UpsellAddonOfferMutation

internal interface GetTravelAddonOfferUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, GenerateAddonOfferResult>
}

internal class GetTravelAddonOfferUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTravelAddonOfferUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, GenerateAddonOfferResult> {
    return either {
      val isAddonFlagOn = featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).first()
      if (!isAddonFlagOn) {
        logcat(LogPriority.ERROR) { "Tried to start UpsellAddonOfferMutation but addon feature flag is off" }
        raise(ErrorMessage())
      }
      apolloClient.mutation(UpsellAddonOfferMutation(id)).safeExecute().fold(
        ifLeft = { error ->
          logcat(LogPriority.ERROR, error) { "Tried to start UpsellAddonOfferMutation but got error: $error" }
          // not passing error message to the member here, as we want to redirect member to chat if there is a message
          raise(ErrorMessage())
        },
        ifRight = { result ->
          if (result.upsellTravelAddonOffer.userError != null) {
            raise(ErrorMessage(result.upsellTravelAddonOffer.userError.message))
            // the only case where we want to redirect to chat
          }
          val data = result.upsellTravelAddonOffer.offer
          if (data == null) {
            logcat(LogPriority.ERROR) { "Tried to do UpsellAddonOfferMutation but got null offer" }
            raise(ErrorMessage())
          }
          val nonEmptyQuotes = data.quotes.toNonEmptyListOrNull()
          if (nonEmptyQuotes.isNullOrEmpty()) {
            logcat(LogPriority.ERROR) { "Tried to do UpsellAddonOfferMutation but got empty quotes" }
            raise(ErrorMessage())
          }
          val addonOffer = Selectable(
            addonOptions = nonEmptyQuotes.toTravelAddonQuotes(),
            selectionTitle = "Title TESTING", //todo add when data allows
            selectionDescription = "Description TESTING",  //todo add when data allows
            fieldTitle = "Maximum travel limit TESTING",  //todo add when data allows
          )
          GenerateAddonOfferResult(
            pageTitle = "Page title TESTING", //todo add when data allows
            pageDescription = "Page description TESTING", //todo add when data allows
            umbrellaAddonQuote = UmbrellaAddonQuote(
              quoteId = data.quotes.firstOrNull()?.quoteId ?: "", //todo when backend allows!
              displayTitle = data.titleDisplayName,
              displayDescription = data.descriptionDisplayName,
              activationDate = data.activationDate,
              addonOffer = addonOffer,
              activeAddons =
                data.currentAddon.toCurrentAddon()?.let {
                  listOf(it)
                } ?: emptyList(), //todo: add list of addons when data allows
              baseInsuranceCost = fakeBaseInsuranceCost, //todo: REMOVE FAKE when data allows
              productVariant = fakeProductVariant  //todo: REMOVE FAKE when data allows
            ),
            currentTotalCost = fakeBaseInsuranceCost  //todo: REMOVE FAKE when data allows
          )
        },
      )
    }
  }
}

val fakeProductVariant = ProductVariant(
  displayName = "productVariant.displayName",
  contractGroup = ContractGroup.CAR,
  contractType = ContractType.SE_CAR_FULL,
  partner = null,
  perils = emptyList(),
  insurableLimits = emptyList(),
  documents = emptyList(),
  displayTierName = "productVariant.displayTierName",
  tierDescription = "productVariant.tierDescription",
  termsVersion = "productVariant.termsVersion"
)
val fakeBaseInsuranceCost = ItemCost(
  monthlyNet = UiMoney(200.0, UiCurrencyCode.SEK),
  monthlyGross = UiMoney(200.0, UiCurrencyCode.SEK),
  discounts = emptyList()
)

private fun NonEmptyList<UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.Quote>.toTravelAddonQuotes():
  NonEmptyList<AddonQuote> {
  return this.map {
    AddonQuote(
      addonId = it.addonId,
      displayTitle = it.displayName,
      displayDetails = it.displayItems.map { item ->
        item.displayTitle to item.displayValue
      },
      addonVariant = it.addonVariant.toAddonVariant(),
      displayDescription = it.displayNameLong,
      itemCost = ItemCost.fromItemCostFragment(it.itemCost),
      documents = emptyList(), //todo: ADD WHEN BACKEND ALLOWS!
      addonSubtype = it.addonSubtype //todo: change WHEN BACKEND ALLOWS!
    )
  }
}

private fun UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.CurrentAddon?.toCurrentAddon():
  CurrentlyActiveAddon? {
  return this?.let {
    CurrentlyActiveAddon(
      displayTitle = displayNameLong,
      displayDescription = "CURRENT ADDON displayDescription", //todo: REMOVE FAKE!
      cost = ItemCost(UiMoney.fromMoneyFragment(netPremium),
        UiMoney.fromMoneyFragment(netPremium),
        emptyList()), //todo: REMOVE FAKE!
    )
  }
}
