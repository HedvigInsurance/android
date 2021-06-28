package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.toMonetaryAmount

// TODO Add extension to BundledQuote and fetch this from BE
private const val GDPR_LINK = "https://www.hedvig.com/se/personuppgifter"

object OfferItemsBuilder {
    fun createTopOfferItems(data: OfferQuery.Data): List<OfferModel> = ArrayList<OfferModel>().apply {
        add(
            OfferModel.Header(
                data.getDisplayNameOrNull(),
                data
                    .quoteBundle
                    .bundleCost
                    .fragments
                    .costFragment
                    .monthlyNet
                    .fragments
                    .monetaryAmountFragment
                    .toMonetaryAmount(),
                data
                    .quoteBundle
                    .bundleCost
                    .fragments
                    .costFragment
                    .monthlyGross
                    .fragments
                    .monetaryAmountFragment
                    .toMonetaryAmount(),
                null
            ),
        )
        add(
            OfferModel.Facts(data.quoteBundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()),
        )
        add(OfferModel.Subheading.Coverage)
        if (data.quoteBundle.quotes.size > 1) {
            add(OfferModel.Paragraph)
            data.quoteBundle.quotes.forEach { quote ->
                add(OfferModel.QuoteDetails(quote.displayName, quote.id))
            }
        } else {
            // We don't have this text, as far as I know
        }
    }
    private fun OfferQuery.Data.getDisplayNameOrNull() = if (quoteBundle.quotes.size == 1) {
        quoteBundle.quotes.firstOrNull()?.displayName
    } else {
        null
    }

    fun createDocumentItems(data: List<OfferQuery.Quote>): List<DocumentItems> {
        if (data.size != 1) {
            return emptyList()
        }
        val documents = data[0].insuranceTerms.map {
            DocumentItems.Document.from(it)
        }
        return listOf(DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)) + documents
    }

    fun createInsurableLimits(data: List<OfferQuery.Quote>) = if (data.size == 1) {
        data[0]
            .insurableLimits
            .map {
                InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
            }
            .let {
                listOf(InsurableLimitItem.Header.Details) + it
            }
    } else {
        emptyList()
    }

    fun createBottomOfferItems() = listOf(
        OfferModel.Footer(GDPR_LINK),
    )

    fun createPerilItems(data: List<OfferQuery.Quote>) = if (data.size == 1) {
        data[0]
            .perils
            .map { peril ->
                peril.fragments.perilFragment.let { perilFragment ->
                    PerilItem.Peril(Peril.from(perilFragment))
                }
            }
    } else {
        emptyList()
    }
}
