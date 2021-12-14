package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDate
import com.hedvig.app.feature.offer.ui.changestartdate.getStartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.checkoutLabel
import com.hedvig.app.feature.offer.ui.gradientType
import com.hedvig.app.feature.offer.ui.grossMonthlyCost
import com.hedvig.app.feature.offer.ui.netMonthlyCost
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.COMPLETE
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.FAILED
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.IN_PROGRESS
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Error
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.minus
import javax.money.MonetaryAmount

object OfferItemsBuilder {
    fun createTopOfferItems(
        offerData: OfferQuery.Data,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
        dataCollectionResult: DataCollectionResult? = null,
    ): List<OfferModel> = TopOfferItemsBuilder.createTopOfferItems(
        offerData,
        dataCollectionStatus,
        dataCollectionResult,
    )

    fun createDocumentItems(data: List<OfferQuery.Quote>): List<DocumentItems> {
        if (data.size != 1) {
            return emptyList()
        }
        val documents = data[0].insuranceTerms.map {
            DocumentItems.Document.from(it.fragments.insuranceTermFragment)
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

    fun createPerilItems(data: List<OfferQuery.Quote>) = if (data.size == 1) {
        data[0]
            .contractPerils
            .map { peril ->
                peril.fragments.perilFragment.let { perilFragment ->
                    PerilItem.Peril(Peril.from(perilFragment))
                }
            }
    } else {
        emptyList()
    }

    fun createBottomOfferItems(
        data: OfferQuery.Data,
    ): List<OfferModel> = BottomOfferItemsBuilder.createBottomOfferItems(data)
}

@OptIn(ExperimentalStdlibApi::class)
object TopOfferItemsBuilder {
    fun createTopOfferItems(
        offerData: OfferQuery.Data,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
        dataCollectionResult: DataCollectionResult? = null,
    ): List<OfferModel> = buildList {
        val bundle = offerData.quoteBundle
        add(
            OfferModel.Header(
                title = bundle.displayName,
                startDate = bundle.inception.getStartDate(),
                startDateLabel = bundle
                    .inception
                    .getStartDateLabel(bundle.appConfiguration.startDateTerminology),
                premium = offerData.finalPremium,
                originalPremium = offerData.grossMonthlyCost(),
                hasDiscountedPrice = !offerData.grossMonthlyCost().isEqualTo(offerData.netMonthlyCost()) &&
                    !bundle.appConfiguration.ignoreCampaigns,
                incentiveDisplayValue = offerData
                    .redeemedCampaigns
                    .mapNotNull { it.fragments.incentiveFragment.displayValue },
                hasCampaigns = offerData.redeemedCampaigns.isNotEmpty(),
                changeDateBottomSheetData = bundle.inception.toChangeDateBottomSheetData(),
                checkoutLabel = offerData.checkoutLabel(),
                signMethod = offerData.signMethodForQuotes,
                approveButtonTerminology = bundle.appConfiguration.approveButtonTerminology,
                showCampaignManagement = bundle.appConfiguration.showCampaignManagement,
                ignoreCampaigns = bundle.appConfiguration.ignoreCampaigns,
                gradientType = offerData.gradientType(),
            ),
        )
        val showInsurelyInformation = dataCollectionStatus != null
        if (showInsurelyInformation) {
            add(OfferModel.PriceComparisonHeader)
            when (dataCollectionStatus) {
                is Error -> {
                    add(OfferModel.InsurelyCard.FailedToRetrieve(dataCollectionStatus.referenceUuid))
                }
                is Content -> {
                    add(mapContentToInsurelyCard(dataCollectionStatus, dataCollectionResult, offerData))
                }
            }
        }
        add(OfferModel.Facts(bundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()))
        add(OfferModel.Subheading.Coverage)
        if (bundle.quotes.size > 1) {
            add(OfferModel.Paragraph.Coverage)
            bundle.quotes.forEach { quote ->
                add(OfferModel.QuoteDetails(quote.displayName, quote.id))
            }
        }
    }

    private fun mapContentToInsurelyCard(
        dataCollectionStatusContent: Content,
        dataCollectionResult: DataCollectionResult?,
        offerData: OfferQuery.Data,
    ): OfferModel.InsurelyCard {
        val referenceUuid = dataCollectionStatusContent.referenceUuid
        val result = dataCollectionStatusContent.dataCollectionStatus

        return when (result.subscriptionStatus) {
            IN_PROGRESS -> OfferModel.InsurelyCard.Loading(referenceUuid, result.insuranceCompany)
            FAILED -> OfferModel.InsurelyCard.FailedToRetrieve(referenceUuid, result.insuranceCompany)
            COMPLETE -> {
                val collectionResult = dataCollectionResult?.collectedList
                if (collectionResult == null) {
                    OfferModel.InsurelyCard.FailedToRetrieve(referenceUuid, result.insuranceCompany)
                } else {
                    val currentInsurances = collectionResult
                        .mapNotNull { collectedInsuranceData ->
                            val name = collectedInsuranceData.name
                            val finalPremium = collectedInsuranceData.netPremium
                            if (name == null || finalPremium == null) return@mapNotNull null
                            OfferModel.InsurelyCard.Retrieved.CurrentInsurance(name, finalPremium)
                        }
                    val ourPremium = offerData.finalPremium
                    val otherPremium = collectionResult
                        .mapNotNull { it.netPremium }
                        .reduceOrNull(MonetaryAmount::add)
                    val savedWithHedvig = otherPremium?.minus(ourPremium)?.takeIf { it.isPositive }
                    OfferModel.InsurelyCard.Retrieved(
                        id = referenceUuid,
                        insuranceProvider = result.insuranceCompany,
                        insurelyDataCollectionReferenceUuid = "",
                        currentInsurances = currentInsurances,
                        savedWithHedvig = savedWithHedvig
                    )
                }
            }
        }
    }

    private val OfferQuery.Data.finalPremium: MonetaryAmount
        get() = if (quoteBundle.appConfiguration.ignoreCampaigns) {
            grossMonthlyCost()
        } else {
            netMonthlyCost()
        }
}

@OptIn(ExperimentalStdlibApi::class)
object BottomOfferItemsBuilder {
    fun createBottomOfferItems(
        offerData: OfferQuery.Data,
    ): List<OfferModel> = buildList {
        val bundle = offerData.quoteBundle
        val showInsuranceSwitchableStates = bundle.quotes.any { quote -> quote.currentInsurer != null }
        if (showInsuranceSwitchableStates) {
            add(OfferModel.Subheading.Switcher(bundle.quotes.count { it.isDisplayable }))
            addAll(currentInsuranceSwitchableStates(bundle.quotes))
        }
        if (bundle.frequentlyAskedQuestions.isNotEmpty() && bundle.appConfiguration.showFAQ) {
            add(
                OfferModel.FAQ(
                    bundle.frequentlyAskedQuestions.mapNotNull { FAQItem.from(it) }
                )
            )
        }
        add(OfferModel.Footer(offerData.checkoutLabel()))
    }

    private fun currentInsuranceSwitchableStates(
        quotes: List<OfferQuery.Quote>,
    ): List<OfferModel> = buildList {
        val nonSwitchables = quotes
            .mapNotNull { quote ->
                quote.currentInsurer?.let { currentInsurer ->
                    if (currentInsurer.switchable == false) {
                        currentInsurer to quote.displayName
                    } else {
                        null
                    }
                }
            }
        if (nonSwitchables.isNotEmpty()) {
            nonSwitchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferModel.CurrentInsurer(
                        displayName = currentInsurer.displayName,
                        associatedQuote = if (quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            add(OfferModel.ManualSwitchCard)
        }
        val switchables = quotes.mapNotNull { quote ->
            quote.currentInsurer?.let { currentInsurer ->
                if (currentInsurer.switchable == true) {
                    currentInsurer to quote.displayName
                } else {
                    null
                }
            }
        }
        if (switchables.isNotEmpty()) {
            switchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferModel.CurrentInsurer(
                        displayName = currentInsurer.displayName,
                        associatedQuote = if (quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            add(OfferModel.AutomaticSwitchCard)
        }
    }

    private val OfferQuery.Quote.isDisplayable: Boolean
        get() = currentInsurer?.displayName != null
}
