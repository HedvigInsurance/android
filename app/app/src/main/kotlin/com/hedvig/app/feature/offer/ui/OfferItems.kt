package com.hedvig.app.feature.offer.ui

import androidx.compose.ui.unit.Dp
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.offer.model.CheckoutLabel
import com.hedvig.app.feature.offer.model.CheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.OfferStartDate
import com.hedvig.app.feature.offer.model.quotebundle.StartDateLabel
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import javax.money.MonetaryAmount

sealed class OfferItems {

  data class Header(
    val title: String?,
    val startDate: OfferStartDate,
    val startDateLabel: StartDateLabel,
    val checkoutLabel: CheckoutLabel,
    val premium: MonetaryAmount,
    val hasDiscountedPrice: Boolean,
    val originalPremium: MonetaryAmount,
    val incentiveDisplayValue: String?,
    val hasCampaigns: Boolean,
    val changeDateBottomSheetData: ChangeDateBottomSheetData,
    val checkoutMethod: CheckoutMethod,
    val showCampaignManagement: Boolean,
    val ignoreCampaigns: Boolean,
    val gradientType: ContractType,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse?,
    val quoteCartId: QuoteCartId?,
  ) : OfferItems()

  object VariantHeader : OfferItems()

  data class VariantButton(
    val id: String,
    val title: String,
    val tag: String?,
    val description: String?,
    val price: MonetaryAmount,
    val isSelected: Boolean,
    val onVariantSelected: (id: String) -> Unit,
  ) : OfferItems()

  data class Facts(
    val table: Table,
  ) : OfferItems()

  data class CurrentInsurer(
    val displayName: String?,
    val associatedQuote: String?,
  ) : OfferItems()

  data class Footer(
    val checkoutLabel: CheckoutLabel,
  ) : OfferItems()

  sealed class Subheading : OfferItems() {
    object Coverage : Subheading()
    data class Switcher(val amountOfCurrentInsurers: Int) : Subheading()
  }

  sealed class Paragraph : OfferItems() {
    object Coverage : Paragraph()
  }

  data class InsurelyDivider(val topPadding: Dp) : OfferItems()

  object PriceComparisonHeader : OfferItems()

  sealed class InsurelyCard : OfferItems() {
    abstract val id: String
    abstract val insuranceProviderDisplayName: String?

    data class Loading(override val id: String, override val insuranceProviderDisplayName: String?) : InsurelyCard()

    data class FailedToRetrieve(
      override val id: String,
      override val insuranceProviderDisplayName: String? = null,
    ) : InsurelyCard()

    data class Retrieved(
      override val id: String,
      override val insuranceProviderDisplayName: String?,
      val currentInsurances: List<CurrentInsurance>,
      val savedWithHedvig: MonetaryAmount?,
    ) : InsurelyCard() {
      val totalNetPremium: MonetaryAmount? = currentInsurances
        .takeIf { it.isNotEmpty() }
        ?.map(CurrentInsurance::amount)
        ?.reduce(MonetaryAmount::add)

      data class CurrentInsurance(
        val name: String,
        val amount: MonetaryAmount,
      )

      companion object
    }
  }

  data class QuoteDetails(
    val name: String,
    val id: String,
  ) : OfferItems()

  data class FAQ(
    val items: List<FAQItem>,
  ) : OfferItems()

  object AutomaticSwitchCard : OfferItems()
  object ManualSwitchCard : OfferItems()
  object Error : OfferItems()
}
