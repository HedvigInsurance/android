package com.hedvig.app.feature.offer

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.COMPLETE
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.FAILED
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.IN_PROGRESS
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Error
import com.hedvig.app.util.minus
import javax.money.MonetaryAmount

object OfferItemsBuilder {
    fun createTopOfferItems(
        offerData: OfferModel,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
        dataCollectionResult: DataCollectionResult? = null,
        insuranceProviderDisplayName: String? = null,
        paymentMethods: PaymentMethodsApiResponse? = null,
    ): List<OfferItems> = TopOfferItemsBuilder.createTopOfferItems(
        offerData,
        dataCollectionStatus,
        dataCollectionResult,
        insuranceProviderDisplayName,
        paymentMethods,
    )

    fun createBottomOfferItems(
        data: OfferModel,
    ): List<OfferItems> = BottomOfferItemsBuilder.createBottomOfferItems(data)
}

@OptIn(ExperimentalStdlibApi::class)
object TopOfferItemsBuilder {
    fun createTopOfferItems(
        offerModel: OfferModel,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
        dataCollectionResult: DataCollectionResult? = null,
        insuranceProviderDisplayName: String?,
        paymentMethods: PaymentMethodsApiResponse?,
    ): List<OfferItems> = buildList {
        val bundle = offerModel.quoteBundle
        add(
            OfferItems.Header(
                title = bundle.name,
                startDate = bundle.inception.startDate,
                startDateLabel = bundle.inception.startDateLabel,
                premium = bundle.cost.finalPremium,
                originalPremium = bundle.cost.grossMonthlyCost,
                hasDiscountedPrice = !bundle.cost.grossMonthlyCost.isEqualTo(bundle.cost.netMonthlyCost) &&
                    !bundle.viewConfiguration.ignoreCampaigns,
                incentiveDisplayValue = offerModel.campaign?.displayValue,
                hasCampaigns = offerModel.campaign?.shouldShowIncentive == true,
                changeDateBottomSheetData = bundle.inception.changeDateData,
                checkoutLabel = offerModel.checkoutLabel,
                checkoutMethod = offerModel.checkoutMethod,
                showCampaignManagement = bundle.viewConfiguration.showCampaignManagement,
                ignoreCampaigns = bundle.viewConfiguration.showCampaignManagement,
                gradientType = bundle.viewConfiguration.gradient,
                paymentMethodsApiResponse = paymentMethods
            ),
        )
        val showInsurelyInformation = dataCollectionStatus != null
        if (showInsurelyInformation) {
            add(OfferItems.PriceComparisonHeader)
            @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT") // https://youtrack.jetbrains.com/issue/KT-51541
            when (dataCollectionStatus) {
                is Error -> {
                    add(
                        OfferItems.InsurelyCard.FailedToRetrieve(
                            id = dataCollectionStatus.referenceUuid,
                            insuranceProviderDisplayName = insuranceProviderDisplayName
                        )
                    )
                }
                is Content -> {
                    add(
                        mapContentToInsurelyCard(
                            dataCollectionStatus,
                            dataCollectionResult,
                            offerModel,
                            insuranceProviderDisplayName,
                        )
                    )
                }
            }
        }
        bundle.quotes.map {
            add(OfferItems.Facts(it.detailsTable))
        }
        add(OfferItems.Subheading.Coverage)
        if (bundle.quotes.size > 1) {
            add(OfferItems.Paragraph.Coverage)
            bundle.quotes.forEach { quote ->
                add(OfferItems.QuoteDetails(quote.displayName, quote.id))
            }
        }
    }

    private fun mapContentToInsurelyCard(
        dataCollectionStatusContent: Content,
        dataCollectionResult: DataCollectionResult?,
        offerModel: OfferModel,
        insuranceProviderDisplayName: String?,
    ): OfferItems.InsurelyCard {
        val referenceUuid = dataCollectionStatusContent.referenceUuid
        val result = dataCollectionStatusContent.dataCollectionStatus

        return when (result.subscriptionStatus) {
            IN_PROGRESS -> OfferItems.InsurelyCard.Loading(referenceUuid, insuranceProviderDisplayName)
            FAILED -> OfferItems.InsurelyCard.FailedToRetrieve(referenceUuid, insuranceProviderDisplayName)
            COMPLETE -> {
                when (dataCollectionResult) {
                    null,
                    is DataCollectionResult.Empty,
                    -> {
                        OfferItems.InsurelyCard.FailedToRetrieve(referenceUuid, insuranceProviderDisplayName)
                    }
                    is DataCollectionResult.Content -> {
                        val collectionResult = dataCollectionResult.collectedList
                        val currentInsurances = collectionResult
                            .mapNotNull { collectedInsuranceData ->
                                val name = collectedInsuranceData.name
                                val finalPremium = collectedInsuranceData.netPremium
                                if (name == null || finalPremium == null) return@mapNotNull null
                                OfferItems.InsurelyCard.Retrieved.CurrentInsurance(name, finalPremium)
                            }
                        val ourPremium = offerModel.quoteBundle.cost.finalPremium
                        val otherPremium = collectionResult
                            .mapNotNull { it.netPremium }
                            .reduceOrNull(MonetaryAmount::add)
                        val savedWithHedvig = otherPremium?.minus(ourPremium)?.takeIf(MonetaryAmount::isPositive)
                        OfferItems.InsurelyCard.Retrieved(
                            id = referenceUuid,
                            insuranceProviderDisplayName = insuranceProviderDisplayName,
                            currentInsurances = currentInsurances,
                            savedWithHedvig = savedWithHedvig,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
object BottomOfferItemsBuilder {
    fun createBottomOfferItems(
        offerData: OfferModel,
    ): List<OfferItems> = buildList {
        val bundle = offerData.quoteBundle

        if (bundle.hasCurrentInsurer()) {
            add(OfferItems.Subheading.Switcher(bundle.numberOfCurrentInsurers()))
            addAll(currentInsuranceSwitchableStates(bundle.quotes))
        }
        if (bundle.frequentlyAskedQuestions.isNotEmpty() &&
            bundle.viewConfiguration.showFAQ
        ) {
            add(
                OfferItems.FAQ(
                    bundle.frequentlyAskedQuestions.mapNotNull { FAQItem.from(it) }
                )
            )
        }
        add(OfferItems.Footer(offerData.checkoutLabel))
    }

    private fun currentInsuranceSwitchableStates(
        quotes: List<QuoteBundle.Quote>,
    ): List<OfferItems> = buildList {
        val nonSwitchables = quotes
            .mapNotNull { quote ->
                quote.currentInsurer?.let { currentInsurer ->
                    if (!currentInsurer.switchable) {
                        currentInsurer to quote.displayName
                    } else {
                        null
                    }
                }
            }
        if (nonSwitchables.isNotEmpty()) {
            nonSwitchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferItems.CurrentInsurer(
                        displayName = currentInsurer.name,
                        associatedQuote = if (quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            add(OfferItems.ManualSwitchCard)
        }
        val switchables = quotes.mapNotNull { quote ->
            quote.currentInsurer?.let { currentInsurer ->
                if (currentInsurer.switchable) {
                    currentInsurer to quote.displayName
                } else {
                    null
                }
            }
        }
        if (switchables.isNotEmpty()) {
            switchables.forEach { (currentInsurer, associatedQuote) ->
                add(
                    OfferItems.CurrentInsurer(
                        displayName = currentInsurer.name,
                        associatedQuote = if (quotes.size > 1) {
                            associatedQuote
                        } else {
                            null
                        }
                    )
                )
            }
            add(OfferItems.AutomaticSwitchCard)
        }
    }
}
