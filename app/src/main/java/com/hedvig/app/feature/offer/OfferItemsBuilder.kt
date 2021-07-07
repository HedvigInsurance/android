package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationGradientOption
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.faq.FAQItem
import com.hedvig.app.feature.offer.ui.grossMonthlyCost
import com.hedvig.app.feature.offer.ui.netMonthlyCost
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.safeLet

object OfferItemsBuilder {
    // TODO Add extension to BundledQuote and fetch this from BE
    const val GDPR_LINK = "https://www.hedvig.com/se/personuppgifter"

    fun createTopOfferItems(data: OfferQuery.Data): List<OfferModel> = ArrayList<OfferModel>().apply {
        add(
            OfferModel.Header(
                title = data.quoteBundle.displayName,
                startDate = data.quoteBundle.inception.getStartDate(),
                startDateLabel = data.quoteBundle.inception.getStartDateLabel(data.quoteBundle.appConfiguration.startDateTerminology),
                netMonthlyCost = data.netMonthlyCost(),
                grossMonthlyCost = data.grossMonthlyCost(),
                incentiveDisplayValue = null,
                changeDateBottomSheetData = data.quoteBundle.inception.toChangeDateBottomSheetData(),
                signMethod = data.signMethodForQuotes,
                showCampaignManagement = data.quoteBundle.appConfiguration.showCampaignManagement,
                gradientRes = when (data.quoteBundle.appConfiguration.gradientOption) {
                    QuoteBundleAppConfigurationGradientOption.GRADIENT_ONE -> R.drawable.gradient_fall_sunset
                    QuoteBundleAppConfigurationGradientOption.GRADIENT_TWO -> R.drawable.gradient_spring_fog
                    QuoteBundleAppConfigurationGradientOption.GRADIENT_THREE -> R.drawable.gradient_summer_sky
                    QuoteBundleAppConfigurationGradientOption.UNKNOWN__ -> R.drawable.gradient_spring_fog
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

    fun createBottomOfferItems(bundle: OfferQuery.QuoteBundle) = ArrayList<OfferModel>().apply {
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
            bundle.quotes.mapNotNull {
                it.currentInsurer?.let { currentInsurer ->
                    currentInsurer to it.displayName
                }
            }.forEach { (currentInsurer, associatedQuote) ->
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
                add(
                    if (currentInsurer.switchable == true) {
                        OfferModel.AutomaticSwitchCard
                    } else {
                        OfferModel.ManualSwitchCard
                    }
                )
            }
        }
        add(OfferModel.Footer(GDPR_LINK))
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
