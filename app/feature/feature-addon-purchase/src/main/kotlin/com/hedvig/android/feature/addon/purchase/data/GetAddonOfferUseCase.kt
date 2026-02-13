package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.AddonGenerateOfferMutation
import octopus.fragment.AddonOfferQuoteFragment

internal interface GetAddonOfferUseCase {
  suspend fun invoke(contractId: String): Either<ErrorMessage, GenerateAddonOfferResult>
}

internal class GetAddonOfferUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetAddonOfferUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, GenerateAddonOfferResult> {
    return either {
      val isAddonFlagOn = featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).first()
      if (!isAddonFlagOn) {
        logcat(LogPriority.ERROR) { "Tried to start UpsellAddonOfferMutation but travel addon feature flag is off" }
        raise(ErrorMessage())
      }
      apolloClient
        .mutation(
          AddonGenerateOfferMutation(contractId),
        )
        .safeExecute().fold(
          ifLeft = { error ->
            logcat(LogPriority.ERROR, error) { "Tried to start AddonGenerateOfferMutation but got error: $error" }
            // not passing error message to the member here, as we want to redirect member to chat if there is a message
            raise(ErrorMessage())
          },
          ifRight = { response: AddonGenerateOfferMutation.Data ->
            when (val result = response.addonGenerateOffer) {
              is AddonGenerateOfferMutation.Data.OtherAddonGenerateOffer -> {
                logcat(LogPriority.ERROR) {
                  "Tried to start AddonGenerateOfferMutation but got addonGenerateOffer.Other"
                }
                raise(ErrorMessage())
              }

              is AddonGenerateOfferMutation.Data.UserErrorAddonGenerateOffer -> {
                logcat(LogPriority.ERROR) {
                  "Tried to start AddonGenerateOfferMutation but got error: $response.addonGenerateOffer.message"
                }
                raise(ErrorMessage(response.addonGenerateOffer.message))
              }

              is AddonGenerateOfferMutation.Data.AddonOfferAddonGenerateOffer -> {
                val addonOffer = when (val addonOfferResult = result.quote.addonOffer) {
                  is AddonGenerateOfferMutation.Data.AddonOfferAddonGenerateOffer.Quote.AddonOfferSelectableAddonOffer -> {
                    val nonEmptyQuotes = result.quote.addonOffer.quotes.toNonEmptyListOrNull()
                    if (nonEmptyQuotes.isNullOrEmpty()) {
                      logcat(LogPriority.ERROR) { "Tried to do AddonGenerateOfferMutation but got empty quotes" }
                      raise(ErrorMessage())
                    } else {
                      Selectable(
                        addonOptions = nonEmptyQuotes.toAddonQuotes(
                          result.quote.productVariant.documents.map {
                            TravelAddonQuoteInsuranceDocument(
                              displayName = it.displayName,
                              url = it.url,
                            )
                          },
                        ),
                        selectionTitle = addonOfferResult.selectionTitle,
                        selectionDescription = addonOfferResult.selectionDescription,
                        fieldTitle = addonOfferResult.fieldTitle,
                      )
                    }
                  }

                  is AddonGenerateOfferMutation.Data.AddonOfferAddonGenerateOffer.Quote.AddonOfferToggleableAddonOffer,
                  -> {
                    val nonEmptyQuotes = result.quote.addonOffer.quotes.toNonEmptyListOrNull()
                    if (nonEmptyQuotes.isNullOrEmpty()) {
                      logcat(LogPriority.ERROR) { "Tried to do AddonGenerateOfferMutation but got empty quotes" }
                      raise(ErrorMessage())
                    } else {
                      AddonOffer.Toggleable(
                        addonOptions = nonEmptyQuotes.toAddonQuotes(
                          result.quote.productVariant.documents.map {
                            TravelAddonQuoteInsuranceDocument(
                              displayName = it.displayName,
                              url = it.url,
                            )
                          },
                        ),
                      )
                    }
                  }

                  is AddonGenerateOfferMutation.Data.AddonOfferAddonGenerateOffer.Quote.OtherAddonOffer -> {
                    logcat(LogPriority.ERROR) { "Unknown AddonOffer" }
                    raise(ErrorMessage())
                  }
                }

                GenerateAddonOfferResult(
                  contractId = contractId,
                  pageTitle = result.pageTitle,
                  pageDescription = result.pageDescription,
                  umbrellaAddonQuote = UmbrellaAddonQuote(
                    quoteId = result.quote.quoteId,
                    displayTitle = result.quote.displayTitle,
                    displayDescription = result.quote.displayDescription,
                    activationDate = result.quote.activationDate,
                    addonOffer = addonOffer,
                    activeAddons = result.quote.activeAddons.toActiveAddons(),
                    baseInsuranceCost = ItemCost.fromItemCostFragment(result.quote.baseQuoteCost),
                    productVariant = result.quote.productVariant.toProductVariant(),
                  ),
                  currentTotalCost = ItemCost.fromItemCostFragment(result.currentTotalCost),
                  notificationMessage = result.infoMessage,
                  whatsIncludedPageTitle = result.whatsIncludedPageTitle,
                  whatsIncludedPageDescription = result.whatsIncludedPageDescription,
                )
              }
            }
          },
        )
    }
  }
}

private fun NonEmptyList<
  AddonOfferQuoteFragment,
>.toAddonQuotes(
  documents: List<TravelAddonQuoteInsuranceDocument>,
): NonEmptyList<AddonQuote> {
  return this.map { addonQuote ->
    AddonQuote(
      addonId = addonQuote.id,
      displayTitle = addonQuote.displayTitle,
      displayDescription = addonQuote.displayDescription,
      displayDetails = addonQuote.displayItems.map { item ->
        item.displayTitle to item.displayValue
      },
      addonVariant = addonQuote.addonVariant.toAddonVariant(),
      itemCost = ItemCost.fromItemCostFragment(addonQuote.cost),
      documents = documents,
      addonSubtype = addonQuote.subtype,
    )
  }
}

private fun List<AddonGenerateOfferMutation.Data.AddonOfferAddonGenerateOffer.Quote.ActiveAddon>.toActiveAddons():
  List<CurrentlyActiveAddon> {
  return this.map { activeAddon ->
    CurrentlyActiveAddon(
      displayTitle = activeAddon.displayTitle,
      displayDescription = activeAddon.displayDescription,
      cost = ItemCost.fromItemCostFragment(activeAddon.cost),
    )
  }
}
