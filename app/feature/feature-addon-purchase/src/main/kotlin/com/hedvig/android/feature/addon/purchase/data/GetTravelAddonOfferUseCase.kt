package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.featureflags.FeatureManager
import kotlinx.datetime.LocalDate

internal interface GetTravelAddonOfferUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer>
}

internal class GetTravelAddonOfferUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetTravelAddonOfferUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer> {
    // todo: REMOVE MOCK!
    return either {
      mockWithoutUpgrade
    }
  }
}

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
        displayDetails = listOf(),
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
        documents = listOf(),
        displayDetails = listOf(),
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
        documents = listOf(),
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
