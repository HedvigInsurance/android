package com.hedvig.app.feature.offer

import android.net.Uri
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.toMonetaryAmount

// TODO Add extension to BundledQuote and fetch this from BE
private const val GDPR_LINK = "https://www.hedvig.com/se/personuppgifter"

object OfferItemsBuilder {

    fun createTopOfferItems(data: OfferQuery.Data) = listOf(
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
            changeDateBottomSheetData = data.quoteBundle.inception.toChangeDateBottomSheetData()
        ),
        OfferModel.Facts(data.quoteBundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()),
        OfferModel.Footer(GDPR_LINK),
    )

    private fun OfferQuery.Data.getDisplayNameOrNull() = if (quoteBundle.quotes.size == 1) {
        quoteBundle.quotes.firstOrNull()?.displayName
    } else {
        null
    }

    fun createDocumentItems(data: OfferQuery.Quote): List<DocumentItems> {
        val documents = data.insuranceTerms.map {
            DocumentItems.Document(
                title = it.displayName,
                subtitle = null,
                uri = Uri.parse(it.url),
                type = DocumentItems.Document.Type.GENERAL_TERMS
            )
        }
        return listOf(DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)) + documents
    }

    fun createInsurableLimits(data: OfferQuery.Quote) = data
        .insurableLimits
        .map {
            it.fragments.insurableLimitsFragment.let { insurableLimitsFragment ->
                InsurableLimitItem.InsurableLimit(
                    label = insurableLimitsFragment.label,
                    limit = insurableLimitsFragment.limit,
                    description = insurableLimitsFragment.description,
                )
            }
        }
        .let {
            listOf(InsurableLimitItem.Header.Details) + it
        }

    fun createBottomOfferItems() = listOf(
        OfferModel.Footer(GDPR_LINK),
    )

    fun createPerilItems(data: OfferQuery.Quote) = data
        .perils
        .map { peril ->
            peril.fragments.perilFragment.let { perilFragment ->
                PerilItem.Peril(Peril.from(perilFragment))
            }
        }

    fun createLoadingItem(): List<OfferModel.Loading> = listOf(OfferModel.Loading)
}
