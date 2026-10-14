package com.hedvig.android.feature.inapppurchase.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.ui.tiersandaddons.CostBreakdownEntry
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class InAppProduct {
  ACCIDENT,
  PAYMENT_PROTECTION,
}

internal data class InAppPurchaseForm(
  val product: InAppPurchaseProduct,
  val eligibility: InAppPurchaseEligibility,
  /**
   * Empty when not eligible.
   */
  val fields: List<InAppPurchaseFormField>,
)

@Serializable
internal data class InAppPurchaseProduct(
  val name: String,
  val displayNameShort: String,
  val displayNameFull: String,
  val tagline: String,
  val pillowImage: InAppPurchaseImage,
  val featuredImage: InAppPurchaseImage?,
  val variants: List<ProductVariant>,
  val faq: List<InAppPurchaseFaqItem>,
)

@Serializable
internal data class InAppPurchaseImage(
  val src: String,
  val alt: String?,
)

@Serializable
internal data class InAppPurchaseFaqItem(
  val question: String,
  val answerMarkdown: String,
)

internal sealed interface InAppPurchaseEligibility {
  data object Eligible : InAppPurchaseEligibility

  data class NotEligible(
    val reason: InAppPurchaseIneligibilityReason,
    val existingContractId: String?,
  ) : InAppPurchaseEligibility
}

internal enum class InAppPurchaseIneligibilityReason {
  ALREADY_INSURED,
  NOT_AVAILABLE,
}

/**
 * A server-driven input. [id] is opaque and only sent back in [InAppPurchaseFieldInput.fieldId].
 */
internal sealed interface InAppPurchaseFormField {
  val id: String
  val title: String

  data class Number(
    override val id: String,
    override val title: String,
    val defaultValue: Int?,
    val minValue: Int?,
    val maxValue: Int?,
  ) : InAppPurchaseFormField

  data class Date(
    override val id: String,
    override val title: String,
    val defaultValue: LocalDate?,
    val minValue: LocalDate?,
    val maxValue: LocalDate?,
  ) : InAppPurchaseFormField

  data class SingleSelect(
    override val id: String,
    override val title: String,
    val options: List<Option>,
    val defaultValue: String?,
  ) : InAppPurchaseFormField {
    data class Option(
      val value: String,
      val title: String,
      val isSelectable: Boolean,
      val additionalInfo: String?,
    )
  }
}

/**
 * NUMBER: "2" · DATE: ISO "2026-11-01" · SINGLE_SELECT: the option's value.
 */
internal data class InAppPurchaseFieldInput(
  val fieldId: String,
  val values: List<String>,
)

internal sealed interface InAppPurchaseQuote {
  /**
   * Offer ids change on every quote, so only an offer from the latest [Quoted] may be signed.
   */
  data class Quoted(
    val shopSessionId: String,
    val offers: List<InAppPurchaseOffer>,
  ) : InAppPurchaseQuote

  data class InvalidFields(
    val errors: List<InAppPurchaseFieldError>,
  ) : InAppPurchaseQuote
}

internal data class InAppPurchaseFieldError(
  val fieldId: String,
  val message: String,
)

@Serializable
internal data class InAppPurchaseOffer(
  val id: String,
  val variant: ProductVariant,
  val exposureDisplayName: String,
  val cost: InAppPurchaseOfferCost,
  val costBreakdown: List<CostBreakdownEntry>,
  val displayItems: List<InAppPurchaseDisplayItem>,
  val deductible: InAppPurchaseDeductible?,
  val insuranceAmount: InAppPurchaseInsuranceAmount?,
  val usps: List<String>,
  val startDate: LocalDate?,
)

@Serializable
internal data class InAppPurchaseOfferCost(
  val baseMonthlyGross: UiMoney,
  val monthlyGross: UiMoney,
  val monthlyNet: UiMoney,
  val discounts: List<InAppPurchaseDiscount>,
)

@Serializable
internal data class InAppPurchaseDiscount(
  val displayName: String,
  /**
   * Monthly reduction, negative.
   */
  val amount: UiMoney,
)

@Serializable
internal data class InAppPurchaseDisplayItem(
  val title: String,
  val value: String,
)

@Serializable
internal data class InAppPurchaseInsuranceAmount(
  val displayName: String,
  val amount: Int,
)

@Serializable
internal data class InAppPurchaseDeductible(
  val displayName: String,
  val tagline: String,
)

/**
 * `ProductOffer` has no breakdown of its own, so one line for the insurance followed by one line per discount.
 */
internal fun costBreakdownOf(productDisplayName: String, cost: InAppPurchaseOfferCost): List<CostBreakdownEntry> {
  return buildList {
    add(CostBreakdownEntry(displayName = productDisplayName, displayValue = cost.baseMonthlyGross))
    cost.discounts.forEach { discount ->
      add(CostBreakdownEntry(displayName = discount.displayName, displayValue = discount.amount))
    }
  }
}

internal data class InAppPurchaseSigning(
  val id: String,
  val status: InAppPurchaseSigningStatus,
  val autoStartToken: String?,
  val liveQrCodeData: String?,
  val bankIdAppOpened: Boolean,
  val failureMessage: String?,
)

internal enum class InAppPurchaseSigningStatus {
  PENDING,
  CREATING,
  SIGNED,
  FAILED,
}
