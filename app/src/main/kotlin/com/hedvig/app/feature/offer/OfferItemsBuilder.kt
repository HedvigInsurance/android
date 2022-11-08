package com.hedvig.app.feature.offer

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.ui.OfferItems

object OfferItemsBuilder {
  fun createTopOfferItems(
    quoteBundleVariant: QuoteBundleVariant,
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
