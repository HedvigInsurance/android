package com.hedvig.app.feature.offer

import android.net.Uri
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.offer.ui.OfferModel

// TODO Add extension to BundledQuote and fetch this from BE
private const val GDPR_LINK = "https://www.hedvig.com/se/personuppgifter"

object OfferItemsBuilder {
    fun createOfferItems(data: OfferQuery.Data): List<OfferModel> {
        return listOfNotNull(
            OfferModel.Header(data),
            OfferModel.Facts(data),
            OfferModel.Perils(data),
            OfferModel.Footer(GDPR_LINK),
        )
    }

    fun createDocumentItems(data: OfferQuery.Data): List<DocumentItems> {
        val documents = data.lastQuoteOfMember.asCompleteQuote?.insuranceTerms?.map {
            DocumentItems.Document(
                title = it.displayName,
                subtitle = null,
                uri = Uri.parse(it.url),
                type = DocumentItems.Document.Type.GENERAL_TERMS
            )
        } ?: listOf()
        return listOf(DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)) + documents
    }

    fun createInsurableLimits(data: OfferQuery.Data) {
        data
            .lastQuoteOfMember
            .asCompleteQuote
            ?.insurableLimits
            ?.map { it.fragments.insurableLimitsFragment }
            ?.let {
                // TODO Create items
            }
    }
}
