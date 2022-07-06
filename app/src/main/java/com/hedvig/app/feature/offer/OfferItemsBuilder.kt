package com.hedvig.app.feature.offer

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.feature.offer.usecase.ExternalProvider
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.COMPLETE
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.FAILED
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.DataCollectionStatus.DataCollectionSubscriptionStatus.IN_PROGRESS
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Error
import com.hedvig.app.util.minus
import javax.money.MonetaryAmount

object OfferItemsBuilder {
  fun createTopOfferItems(
    quoteBundleVariant: QuoteBundleVariant,
    externalProvider: ExternalProvider?,
    paymentMethods: PaymentMethodsApiResponse?,
    onVariantSelected: (id: String) -> Unit,
    offerModel: OfferModel,
  ): List<OfferItems> = buildList {
    val bundle = quoteBundleVariant.bundle
    add(
      OfferItems.Header(
        title = bundle.name,
        startDate = bundle.inception.startDate,
        startDateLabel = bundle.inception.startDateLabel,
        premium = bundle.cost.finalPremium,
        hasDiscountedPrice = !bundle.cost.grossMonthlyCost.isEqualTo(bundle.cost.netMonthlyCost) &&
          !bundle.viewConfiguration.ignoreCampaigns,
        originalPremium = bundle.cost.grossMonthlyCost,
        incentiveDisplayValue = offerModel.campaign?.displayValue,
        hasCampaigns = offerModel.campaign?.shouldShowIncentive == true,
        changeDateBottomSheetData = bundle.inception.changeDateData,
        checkoutLabel = bundle.checkoutLabel,
        checkoutMethod = offerModel.checkoutMethod,
        showCampaignManagement = bundle.viewConfiguration.showCampaignManagement,
        ignoreCampaigns = bundle.viewConfiguration.showCampaignManagement,
        gradientType = bundle.viewConfiguration.gradient,
        paymentMethodsApiResponse = paymentMethods,
        quoteCartId = offerModel.id,
      ),
    )

    if (offerModel.variants.size > 1) {
      add(OfferItems.VariantHeader)
      offerModel.variants.forEach {
        add(
          OfferItems.VariantButton(
            id = it.id,
            title = it.title,
            tag = it.tag,
            description = it.description,
            price = it.bundle.cost.finalPremium,
            isSelected = it.id == quoteBundleVariant.id,
            onVariantSelected = onVariantSelected,
          ),
        )
      }
    }

    if (externalProvider != null) {
      add(OfferItems.PriceComparisonHeader)
      when (externalProvider.dataCollectionStatus) {
        is Error -> {
          add(
            OfferItems.InsurelyCard.FailedToRetrieve(
              id = externalProvider.dataCollectionStatus.referenceUuid,
              insuranceProviderDisplayName = externalProvider.insuranceProviderDisplayName,
            ),
          )
        }
        is Content -> {
          add(
            mapContentToInsurelyCard(
              externalProvider.dataCollectionStatus,
              externalProvider.dataCollectionResult,
              bundle.cost.finalPremium,
              externalProvider.insuranceProviderDisplayName,
            ),
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
    ourPremium: MonetaryAmount,
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

  fun createBottomOfferItems(
    bundleVariant: QuoteBundleVariant,
  ): List<OfferItems> = buildList {
    val bundle = bundleVariant.bundle
    if (bundle.hasCurrentInsurer()) {
      add(OfferItems.Subheading.Switcher(bundle.numberOfCurrentInsurers()))
      addAll(currentInsuranceSwitchableStates(bundle.quotes))
    }
    if (bundle.frequentlyAskedQuestions.isNotEmpty() &&
      bundle.viewConfiguration.showFAQ
    ) {
      add(OfferItems.FAQ(bundle.frequentlyAskedQuestions.mapNotNull { FAQItem.from(it) }))
    }
    add(OfferItems.Footer(bundleVariant.bundle.checkoutLabel))
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
            },
          ),
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
            },
          ),
        )
      }
      add(OfferItems.AutomaticSwitchCard)
    }
  }
}
