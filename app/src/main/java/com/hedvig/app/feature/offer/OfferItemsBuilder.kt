package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.checkoutTextRes
import com.hedvig.app.feature.offer.ui.faq.FAQItem
import com.hedvig.app.feature.offer.ui.grossMonthlyCost
import com.hedvig.app.feature.offer.ui.netMonthlyCost
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.minus
import com.hedvig.app.util.safeLet

object OfferItemsBuilder {
    fun createTopOfferItems(data: OfferQuery.Data): List<OfferModel> = ArrayList<OfferModel>().apply {
        add(
            OfferModel.Header(
                title = data.quoteBundle.displayName,
                startDate = data.quoteBundle.inception.getStartDate(),
                startDateLabel = data
                    .quoteBundle
                    .inception
                    .getStartDateLabel(data.quoteBundle.appConfiguration.startDateTerminology),
                premium = if (data.quoteBundle.appConfiguration.ignoreCampaigns) {
                    data.grossMonthlyCost()
                } else {
                    data.netMonthlyCost()
                },
                originalPremium = data.grossMonthlyCost(),
                hasDiscountedPrice = !data.grossMonthlyCost().isEqualTo(data.netMonthlyCost()) &&
                    !data.quoteBundle.appConfiguration.ignoreCampaigns,
                incentiveDisplayValue = data
                    .redeemedCampaigns
                    .mapNotNull { it.fragments.incentiveFragment.displayValue },
                hasCampaigns = data.redeemedCampaigns.isNotEmpty(),
                changeDateBottomSheetData = data.quoteBundle.inception.toChangeDateBottomSheetData(),
                checkoutTextRes = data.checkoutTextRes(),
                signMethod = data.signMethodForQuotes,
                approveButtonTerminology = data.quoteBundle.appConfiguration.approveButtonTerminology,
                showCampaignManagement = data.quoteBundle.appConfiguration.showCampaignManagement,
                ignoreCampaigns = data.quoteBundle.appConfiguration.ignoreCampaigns,
                gradientRes = when (data.quoteBundle.appConfiguration.gradientOption) {
                    TypeOfContractGradientOption.GRADIENT_ONE -> R.drawable.gradient_fall_sunset
                    TypeOfContractGradientOption.GRADIENT_TWO -> R.drawable.gradient_spring_fog
                    TypeOfContractGradientOption.GRADIENT_THREE -> R.drawable.gradient_summer_sky
                    TypeOfContractGradientOption.UNKNOWN__ -> R.drawable.gradient_spring_fog
                },
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

    fun createBottomOfferItems(data: OfferQuery.Data) = ArrayList<OfferModel>().apply {
        val bundle = data.quoteBundle
        if (bundle.frequentlyAskedQuestions.isNotEmpty() && bundle.appConfiguration.showFAQ) {
            add(
                OfferModel.FAQ(
                    bundle.frequentlyAskedQuestions.mapNotNull {
                        safeLet(it.headline, it.body) { headline, body ->
                            FAQItem(headline, body)
                        }
                    }
                )
            )
        }
        if (bundle.quotes.any { it.currentInsurer != null }) {
            add(OfferModel.Subheading.Switcher(bundle.quotes.count { it.currentInsurer?.displayName != null }))
            val nonSwitchables = bundle.quotes.mapNotNull {
                it.currentInsurer?.let { currentInsurer ->
                    if (currentInsurer.switchable == false) {
                        currentInsurer to it.displayName
                    } else {
                        null
                    }
                }
            }
            nonSwitchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferModel.CurrentInsurer(
                        displayName = currentInsurer.displayName,
                        associatedQuote = if (bundle.quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            if (nonSwitchables.isNotEmpty()) {
                add(OfferModel.ManualSwitchCard)
            }
            val switchables = bundle.quotes.mapNotNull {
                it.currentInsurer?.let { currentInsurer ->
                    if (currentInsurer.switchable == true) {
                        currentInsurer to it.displayName
                    } else {
                        null
                    }
                }
            }
            switchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferModel.CurrentInsurer(
                        displayName = currentInsurer.displayName,
                        associatedQuote = if (bundle.quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            if (switchables.isNotEmpty()) {
                add(OfferModel.AutomaticSwitchCard)
            }
        }
        add(OfferModel.Footer(data.checkoutTextRes()))
    }

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
