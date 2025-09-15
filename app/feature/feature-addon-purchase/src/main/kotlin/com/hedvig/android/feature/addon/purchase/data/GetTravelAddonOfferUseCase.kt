package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.UpsellAddonOfferMutation

internal interface GetTravelAddonOfferUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer>
}

internal class GetTravelAddonOfferUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTravelAddonOfferUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer> {
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

          TravelAddonOffer(
            addonOptions = nonEmptyQuotes.toTravelAddonQuotes(),
            title = data.titleDisplayName,
            description = data.descriptionDisplayName,
            activationDate = data.activationDate,
            currentTravelAddon = data.currentAddon.toCurrentAddon(),
          )
        },
      )
    }
  }
}

private fun NonEmptyList<UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.Quote>.toTravelAddonQuotes():
  NonEmptyList<TravelAddonQuote> {
  return this.map {
    TravelAddonQuote(
      addonSubtype = it.addonSubtype,
      quoteId = it.quoteId,
      addonId = it.addonId,
      displayName = it.displayName,
      displayDetails = it.displayItems.map { item ->
        item.displayTitle to item.displayValue
      },
      addonVariant = it.addonVariant.toAddonVariant(),
      documents = it.documents.map { doc ->
        TravelAddonQuoteInsuranceDocument(
          doc.displayName,
          doc.url,
        )
      },
      displayNameLong = it.displayNameLong,
      itemCost = ItemCost.fromItemCostFragment(it.itemCost),
    )
  }
}

private fun UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.CurrentAddon?.toCurrentAddon():
  CurrentTravelAddon? {
  return this?.let {
    CurrentTravelAddon(
      displayDetails = displayItems.map {
        it.displayTitle to it.displayValue
      },
      displayNameLong = displayNameLong,
      netPremium = UiMoney.fromMoneyFragment(netPremium),
    )
  }
}
