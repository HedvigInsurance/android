package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.String
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
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
      } else {
        apolloClient.mutation(UpsellAddonOfferMutation(id)).safeExecute().fold(
          ifLeft = { error ->
            logcat(LogPriority.ERROR) { "Tried to start UpsellAddonOfferMutation but got error: $error" }
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
}

private fun NonEmptyList<UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.Quote>.toTravelAddonQuotes(): NonEmptyList<TravelAddonQuote> {
  return this.map {
    TravelAddonQuote(
      quoteId = it.quoteId,
      addonId = it.addonId,
      displayName = it.displayName,
      price = UiMoney.fromMoneyFragment(it.premium),
      addonVariant = AddonVariant(
        documents = listOf(), // todo: Addons - populate when api changes!
        termsVersion = "", // todo: Addons - populate when api changes!
        displayDetails = it.displayItems.map { item ->
          item.displayTitle to item.displayValue
        },
      ),
    )
  }
}

private fun UpsellAddonOfferMutation.Data.UpsellTravelAddonOffer.Offer.CurrentAddon?.toCurrentAddon(): CurrentTravelAddon? {
  return this?.let {
    CurrentTravelAddon(
      price = UiMoney.fromMoneyFragment(premium),
      displayDetails = displayItems.map {
        it.displayTitle to it.displayValue
      },
    )
  }
}

// todo: remove mocks when not needed
private val mockWithoutUpgrade = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    TravelAddonQuote(
      quoteId = "id",
      addonId = "addonId1",
      displayName = "45 days",
      addonVariant = AddonVariant(
        termsVersion = "terms",
        documents = listOf(
          InsuranceVariantDocument(
            "Terms and conditions",
            "www.url.com",
            InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
          ),
        ),
        displayDetails = listOf("Coverage" to "45 days", "Insured people" to "You+1"),
      ),
      price = UiMoney(
        49.0,
        UiCurrencyCode.SEK,
      ),
    ),
    TravelAddonQuote(
      displayName = "60 days",
      addonId = "addonId1",
      quoteId = "id",
      addonVariant = AddonVariant(
        termsVersion = "terms",
        documents = listOf(
          InsuranceVariantDocument(
            "Terms and conditions",
            "www.url.com",
            InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
          ),
        ),
        displayDetails = listOf("Coverage" to "60 days", "Insured people" to "You+1"),
      ),
      price = UiMoney(
        60.0,
        UiCurrencyCode.SEK,
      ),
    ),
  ),
  title = "Travel plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2025, 1, 1),
  currentTravelAddon = null,
)

private val mockWithUpgrade = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    TravelAddonQuote(
      displayName = "60 days",
      addonId = "addonId1",
      quoteId = "id",
      addonVariant = AddonVariant(
        termsVersion = "terms",
        documents = listOf(
          InsuranceVariantDocument(
            "Terms and conditions",
            "www.url.com",
            InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS,
          ),
        ),
        displayDetails = listOf("Coverage" to "60 days", "Insured people" to "You+1"),
      ),
      price = UiMoney(
        60.0,
        UiCurrencyCode.SEK,
      ),
    ),
  ),
  title = "Travel plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2025, 1, 1),
  currentTravelAddon = CurrentTravelAddon(
    UiMoney(49.0, UiCurrencyCode.SEK),
    listOf("Coverage" to "45 days", "Insured people" to "You+1"),
  ),
)
