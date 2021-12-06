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
        externalInsuranceData,
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

    fun createBottomOfferItems(
        data: OfferQuery.Data,
    ) = ArrayList<OfferModel>().apply {
        val bundle = data.quoteBundle
        if (bundle.frequentlyAskedQuestions.isNotEmpty() && bundle.appConfiguration.showFAQ) {
            add(
                OfferModel.FAQ(
                    bundle.frequentlyAskedQuestions.mapNotNull { FAQItem.from(it) }
                )
            )
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
        val showInsuranceSwitchableStates = bundle.quotes.any { quote -> quote.currentInsurer != null }
        if (showInsurelyInformation || showInsuranceSwitchableStates) {
            add(OfferModel.CurrentInsurancesHeader(bundle.quotes.count { quote -> quote.isDisplayable }))
        }
        if (showInsurelyInformation) {
            when (dataCollectionStatus) {
                is Error -> {
                    add(OfferModel.InsurelyCard.FailedToRetrieve(dataCollectionStatus.referenceUuid))
                }
                is Content -> {
                    add(mapContentToInsurelyCard(dataCollectionStatus, externalInsuranceData, offerData))
                }
            }
        }
        if (showInsuranceSwitchableStates) {
            addAll(currentInsuranceSwitchableStates(bundle.quotes))
        }
        add(
            OfferModel.Facts(bundle.quotes[0].detailsTable.fragments.tableFragment.intoTable()),
        )
        add(OfferModel.Subheading.Coverage)
        if (bundle.quotes.size > 1) {
            add(OfferModel.Paragraph.Coverage)
            bundle.quotes.forEach { quote ->
                add(OfferModel.QuoteDetails(quote.displayName, quote.id))
            }
        }
    }

    private fun mapContentToInsurelyCard(
        content: Content,
        externalInsuranceData: DataCollectionResultQuery.Data?,
        offerData: OfferQuery.Data,
    ): OfferModel.InsurelyCard {
        val referenceUuid = content.referenceUuid
        val result = content.dataCollectionResult

        return when (result.status) {
            IN_PROGRESS -> OfferModel.InsurelyCard.Loading(referenceUuid, result.insuranceCompany)
            FAILED -> OfferModel.InsurelyCard.FailedToRetrieve(referenceUuid, result.insuranceCompany)
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
                    OfferModel.InsurelyCard.FailedToRetrieve(referenceUuid, result.insuranceCompany)
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

    @OptIn(ExperimentalStdlibApi::class)
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
        if (nonSwitchables.isNotEmpty()) {
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
        if (switchables.isNotEmpty()) {
            add(OfferModel.AutomaticSwitchCard)
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

    private val OfferQuery.Quote.isDisplayable: Boolean
        get() = currentInsurer?.displayName != null
}
