package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.faq.FAQItem
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.safeLet

object OfferItemsBuilder {
    // TODO Add extension to BundledQuote and fetch this from BE
    const val GDPR_LINK = "https://www.hedvig.com/se/personuppgifter"

    fun createTopOfferItems(data: OfferQuery.Data): List<OfferModel> = ArrayList<OfferModel>().apply {
        add(
            OfferModel.Header(
                title = data.getDisplayNameOrNull(),
                startDate = data.quoteBundle.inception.getStartDate(),
                startDateLabel = data.quoteBundle.inception.getStartDateLabel(),
                netMonthlyCost = data
                    .quoteBundle
                    .bundleCost
                    .fragments
                    .costFragment
                    .monthlyNet
                    .fragments
                    .monetaryAmountFragment
                    .toMonetaryAmount(),
                grossMonthlyCost = data
                    .quoteBundle
                    .bundleCost
                    .fragments
                    .costFragment
                    .monthlyGross
                    .fragments
                    .monetaryAmountFragment
                    .toMonetaryAmount(),
                incentiveDisplayValue = null,
                changeDateBottomSheetData = data.quoteBundle.inception.toChangeDateBottomSheetData(),
                signMethod = data.signMethodForQuotes
            ),
        )
        add(
            OfferModel.Facts(data.quoteBundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()),
        )
        add(OfferModel.Subheading.Coverage)
        if (data.quoteBundle.quotes.size > 1) {
            add(OfferModel.Paragraph.Coverage)
            data.quoteBundle.quotes.forEach { quote ->
                add(OfferModel.QuoteDetails(quote.displayName, quote.id))
            }
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

    fun createBottomOfferItems(bundle: OfferQuery.QuoteBundle) = listOfNotNull(
        if (bundle.frequentlyAskedQuestions.isNotEmpty()) {
            OfferModel.FAQ(
                bundle.frequentlyAskedQuestions.mapNotNull {
                    safeLet(it.headline, it.body) { headline, body ->
                        FAQItem(headline, body)
                    }
                }
            )
        } else {
            null
        },
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

    fun createLoadingItem(): List<OfferModel.Loading> = listOf(OfferModel.Loading)
}
