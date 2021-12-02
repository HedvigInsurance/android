package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.DataCollectionResultQuery
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
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.DataCollectionResult.DataCollectionStatus.COMPLETE
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.DataCollectionResult.DataCollectionStatus.FAILED
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.DataCollectionResult.DataCollectionStatus.IN_PROGRESS
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.SubscribeToDataCollectionUseCase
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.SubscribeToDataCollectionUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.insurelydatacollection.SubscribeToDataCollectionUseCase.Status.Error
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.minus
import javax.money.MonetaryAmount

object OfferItemsBuilder {
    fun createTopOfferItems(
        offerData: OfferQuery.Data,
        dataCollectionStatus: SubscribeToDataCollectionUseCase.Status? = null,
        externalInsuranceData: DataCollectionResultQuery.Data? = null,
    ): List<OfferModel> = TopOfferItemsBuilder.createTopOfferItems(
        offerData,
        dataCollectionStatus,
        externalInsuranceData
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

    fun createBottomOfferItems(data: OfferQuery.Data) = ArrayList<OfferModel>().apply {
        val bundle = data.quoteBundle
        if (bundle.frequentlyAskedQuestions.isNotEmpty() && bundle.appConfiguration.showFAQ) {
            add(
                OfferModel.FAQ(
                    bundle.frequentlyAskedQuestions.mapNotNull { FAQItem.from(it) }
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
        add(OfferModel.Footer(data.checkoutLabel()))
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
}

object TopOfferItemsBuilder {
    fun createTopOfferItems(
        offerData: OfferQuery.Data,
        dataCollectionStatus: SubscribeToDataCollectionUseCase.Status? = null,
        externalInsuranceData: DataCollectionResultQuery.Data? = null,
    ): List<OfferModel> = ArrayList<OfferModel>().apply {
        add(
            OfferModel.Header(
                title = offerData.quoteBundle.displayName,
                startDate = offerData.quoteBundle.inception.getStartDate(),
                startDateLabel = offerData
                    .quoteBundle
                    .inception
                    .getStartDateLabel(offerData.quoteBundle.appConfiguration.startDateTerminology),
                premium = offerData.finalPremium,
                originalPremium = offerData.grossMonthlyCost(),
                hasDiscountedPrice = !offerData.grossMonthlyCost().isEqualTo(offerData.netMonthlyCost()) &&
                    !offerData.quoteBundle.appConfiguration.ignoreCampaigns,
                incentiveDisplayValue = offerData
                    .redeemedCampaigns
                    .mapNotNull { it.fragments.incentiveFragment.displayValue },
                hasCampaigns = offerData.redeemedCampaigns.isNotEmpty(),
                changeDateBottomSheetData = offerData.quoteBundle.inception.toChangeDateBottomSheetData(),
                checkoutLabel = offerData.checkoutLabel(),
                signMethod = offerData.signMethodForQuotes,
                approveButtonTerminology = offerData.quoteBundle.appConfiguration.approveButtonTerminology,
                showCampaignManagement = offerData.quoteBundle.appConfiguration.showCampaignManagement,
                ignoreCampaigns = offerData.quoteBundle.appConfiguration.ignoreCampaigns,
                gradientType = offerData.gradientType(),
            ),
        )
        if (dataCollectionStatus != null) {
            when (dataCollectionStatus) {
                is Error -> {
                    add(OfferModel.InsurelyHeader(dataCollectionStatus.id))
                    add(OfferModel.InsurelyCard.FailedToRetrieve(dataCollectionStatus.id))
                }
                is Content -> {
                    addAll(
                        mapContentToInsurelyViewModels(
                            dataCollectionStatus,
                            externalInsuranceData,
                            offerData
                        )
                    )
                }
            }
        }
        add(
            OfferModel.Facts(offerData.quoteBundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()),
        )
        add(OfferModel.Subheading.Coverage)
        if (offerData.quoteBundle.quotes.size > 1) {
            add(OfferModel.Paragraph.Coverage)
            offerData.quoteBundle.quotes.forEach { quote ->
                add(OfferModel.QuoteDetails(quote.displayName, quote.id))
            }
        }
    }

    private fun mapContentToInsurelyViewModels(
        content: Content,
        externalInsuranceData: DataCollectionResultQuery.Data?,
        offerData: OfferQuery.Data,
    ): List<OfferModel> {
        val id = content.id
        val result = content.dataCollectionResult

        return when (result.status) {
            IN_PROGRESS -> listOf(
                OfferModel.InsurelyHeader(id),
                OfferModel.InsurelyCard.Loading(id, result.insuranceCompany)
            )
            FAILED -> listOf(
                OfferModel.InsurelyHeader(id),
                OfferModel.InsurelyCard.FailedToRetrieve(id, result.insuranceCompany)
            )
            COMPLETE -> {
                val collectedData: List<DataCollectionResultQuery.DataCollectionV2InsuranceDataCollectionV2>? =
                    externalInsuranceData
                        ?.externalInsuranceProvider
                        ?.dataCollectionV2
                        ?.mapNotNull { data ->
                            when {
                                data.asHouseInsuranceCollection != null -> data.asHouseInsuranceCollection
                                data.asPersonTravelInsuranceCollection != null -> data.asPersonTravelInsuranceCollection
                                else -> null
                            }
                        }
                if (collectedData == null) {
                    listOf(
                        OfferModel.InsurelyHeader(id),
                        OfferModel.InsurelyCard.FailedToRetrieve(id, result.insuranceCompany)
                    )
                } else {
                    val currentInsurances = collectedData
                        .mapNotNull { externalInsurance ->
                            val name = externalInsurance.name
                            val finalPremium = externalInsurance.netPremium
                            if (name == null || finalPremium == null) return@mapNotNull null
                            OfferModel.InsurelyCard.Retrieved.CurrentInsurance(name, finalPremium)
                        }
                    val ourPremium = offerData.finalPremium
                    val otherPremium = collectedData
                        .mapNotNull { it.netPremium }
                        .reduceOrNull(MonetaryAmount::add)
                    val savedWithHedvig = otherPremium?.minus(ourPremium)?.takeIf { it.isPositive }
                    listOf(
                        OfferModel.InsurelyHeader(id, currentInsurances.size),
                        OfferModel.InsurelyCard.Retrieved(
                            id = id,
                            insuranceProvider = result.insuranceCompany,
                            currentInsurances = currentInsurances,
                            savedWithHedvig = savedWithHedvig,
                        )
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

    private val DataCollectionResultQuery.DataCollectionV2InsuranceDataCollectionV2.netPremium: MonetaryAmount?
        get() = when (this) {
            is DataCollectionResultQuery.AsHouseInsuranceCollection -> {
                this.monthlyNetPremium?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
            }
            is DataCollectionResultQuery.AsPersonTravelInsuranceCollection -> {
                this.monthlyNetPremium?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
            }
            else -> null
        }

    private val DataCollectionResultQuery.DataCollectionV2InsuranceDataCollectionV2.name: String?
        get() = when (this) {
            is DataCollectionResultQuery.AsHouseInsuranceCollection -> {
                this.insuranceName
            }
            is DataCollectionResultQuery.AsPersonTravelInsuranceCollection -> {
                this.insuranceName
            }
            else -> null
        }
}
