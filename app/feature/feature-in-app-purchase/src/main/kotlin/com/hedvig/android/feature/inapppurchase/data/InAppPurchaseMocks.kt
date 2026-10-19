package com.hedvig.android.feature.inapppurchase.data

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import kotlinx.datetime.LocalDate

internal const val MockNetworkDelay = 600L

/**
 * The backend's sample responses for the draft `inAppPurchase*` API with their texts in English, returned by the use
 * cases until the API exists. Also used as preview and test data.
 */
internal object InAppPurchaseMocks {
  private const val FIELD_CO_INSURED = "numberCoInsured"
  private const val FIELD_START_DATE = "startDate"
  private const val FIELD_EMPLOYMENT_TYPE = "employmentType"

  private val today = LocalDate(2026, 10, 14)
  private val maxStartDate = LocalDate(2027, 10, 14)

  private var signingPollCount = 0

  // region form

  private val startDateField = InAppPurchaseFormField.Date(
    id = FIELD_START_DATE,
    title = "Start date",
    defaultValue = today,
    minValue = today,
    maxValue = maxStartDate,
  )

  val accidentForm: InAppPurchaseForm
    get() = InAppPurchaseForm(
      product = accidentProduct,
      eligibility = InAppPurchaseEligibility.Eligible,
      fields = listOf(
        InAppPurchaseFormField.Number(
          id = FIELD_CO_INSURED,
          title = "How many more people do you want to insure?",
          defaultValue = 1,
          minValue = 0,
          maxValue = 5,
        ),
        startDateField,
      ),
    )

  val paymentProtectionForm: InAppPurchaseForm
    get() = InAppPurchaseForm(
      product = paymentProtectionProduct,
      eligibility = InAppPurchaseEligibility.Eligible,
      fields = listOf(
        InAppPurchaseFormField.SingleSelect(
          id = FIELD_EMPLOYMENT_TYPE,
          title = "What is your employment?",
          options = listOf(
            option("PERMANENT_EMPLOYED_FULLTIME", "Permanent, full-time"),
            option("PERMANENT_EMPLOYED_PARTTIME", "Permanent, part-time"),
            option(
              value = "CONTRACT_EMPLOYED",
              title = "Fixed-term contract",
              additionalInfo =
                "Covers sick leave. Unemployment is only covered if you have been employed for at least " +
                  "12 months.",
            ),
            option("SELF_EMPLOYED", "Self-employed"),
            option(
              value = "UNEMPLOYED",
              title = "Unemployed",
              isSelectable = false,
              additionalInfo = "Payment Protection requires you to be working when the insurance starts.",
            ),
          ),
          defaultValue = null,
        ),
        startDateField,
      ),
    )

  val accidentAlreadyInsuredForm: InAppPurchaseForm
    get() = InAppPurchaseForm(
      product = accidentProduct,
      eligibility = InAppPurchaseEligibility.NotEligible(
        reason = InAppPurchaseIneligibilityReason.ALREADY_INSURED,
        existingContractId = "6f1c2a9e-3b7d-4c51-9a0e-2d8b5f4e7c13",
      ),
      fields = emptyList(),
    )

  val paymentProtectionNotAvailableForm: InAppPurchaseForm
    get() = InAppPurchaseForm(
      product = paymentProtectionProduct,
      eligibility = InAppPurchaseEligibility.NotEligible(
        reason = InAppPurchaseIneligibilityReason.NOT_AVAILABLE,
        existingContractId = null,
      ),
      fields = emptyList(),
    )

  // endregion

  // region quote

  const val NO_REGISTERED_ADDRESS_MESSAGE =
    "We couldn't find a registered address for you. Contact us and we'll help you."

  fun quote(product: InAppProduct, fields: List<InAppPurchaseFieldInput>): InAppPurchaseQuote {
    val values = fields.associate { it.fieldId to it.values.firstOrNull() }
    val startDate = values[FIELD_START_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val coInsured = values[FIELD_CO_INSURED]?.toIntOrNull()
    val errors = buildList {
      if (startDate == null || startDate < today || startDate > maxStartDate) {
        add(InAppPurchaseFieldError(FIELD_START_DATE, "Choose a date from today onwards."))
      }
      when (product) {
        InAppProduct.ACCIDENT -> {
          if (coInsured == null || coInsured !in 0..5) {
            add(InAppPurchaseFieldError(FIELD_CO_INSURED, "Choose between 0 and 5 co-insured."))
          }
        }

        InAppProduct.PAYMENT_PROTECTION -> {
          val selectable = paymentProtectionForm.fields
            .filterIsInstance<InAppPurchaseFormField.SingleSelect>()
            .single()
            .options
            .filter { it.isSelectable }
            .map { it.value }
          if (values[FIELD_EMPLOYMENT_TYPE] !in selectable) {
            add(
              InAppPurchaseFieldError(
                FIELD_EMPLOYMENT_TYPE,
                "Payment Protection requires you to be working when the insurance starts.",
              ),
            )
          }
        }
      }
    }
    if (errors.isNotEmpty() || startDate == null) return InAppPurchaseQuote.InvalidFields(errors)
    return when (product) {
      InAppProduct.ACCIDENT -> accidentQuote(numberCoInsured = coInsured ?: 0, startDate = startDate)
      InAppProduct.PAYMENT_PROTECTION -> paymentProtectionQuote(startDate)
    }
  }

  /**
   * 79 kr per insured person, with the app's 50% campaign discount.
   */
  fun accidentQuote(numberCoInsured: Int = 1, startDate: LocalDate = today): InAppPurchaseQuote.Quoted {
    val gross = 79.0 * (numberCoInsured + 1)
    return InAppPurchaseQuote.Quoted(
      shopSessionId = "0b6d8f1e-5a2c-4e7b-8c39-71f4a2d6e905",
      offers = listOf(
        offer(
          id = "c3e1a7b4-92d0-4f6a-b815-4e2f9c7d3a10",
          variant = accidentVariant,
          exposureDisplayName = if (numberCoInsured == 0) "You" else "You + $numberCoInsured",
          gross = gross,
          discount = InAppPurchaseDiscount("50% discount", sek(-gross / 2)),
          insuranceAmount = null,
          startDate = startDate,
        ),
      ),
    )
  }

  fun paymentProtectionQuote(startDate: LocalDate = LocalDate(2026, 11, 1)): InAppPurchaseQuote.Quoted {
    return InAppPurchaseQuote.Quoted(
      shopSessionId = "4a9e2c71-0d3b-4f85-a6e2-9b1c7d5f3e28",
      offers = listOf(
        offer(
          id = "8d2f4b6a-1c3e-4a59-9e70-5b8c2d1f6a43",
          variant = paymentProtectionVariant.copy(displayTierName = "6 000 kr"),
          exposureDisplayName = "One person",
          gross = 199.0,
          discount = InAppPurchaseDiscount("Bundle discount", sek(-30.0)),
          insuranceAmount = InAppPurchaseInsuranceAmount("6 000 kr", 6000),
          startDate = startDate,
        ),
        offer(
          id = "f71b3c95-6e2a-4d08-8b4f-3a9d6c2e1b57",
          variant = paymentProtectionVariant.copy(
            displayTierName = "12 000 kr",
            termsVersion = "SE_PAYMENT_PROTECTION_HIGH_20260401",
          ),
          exposureDisplayName = "One person",
          gross = 349.0,
          discount = InAppPurchaseDiscount("Bundle discount", sek(-52.0)),
          insuranceAmount = InAppPurchaseInsuranceAmount("12 000 kr", 12000),
          startDate = startDate,
        ),
      ),
    )
  }

  private fun offer(
    id: String,
    variant: ProductVariant,
    exposureDisplayName: String,
    gross: Double,
    discount: InAppPurchaseDiscount,
    insuranceAmount: InAppPurchaseInsuranceAmount?,
    startDate: LocalDate,
  ): InAppPurchaseOffer {
    val cost = InAppPurchaseOfferCost(
      baseMonthlyGross = sek(gross),
      monthlyGross = sek(gross),
      monthlyNet = sek(gross + discount.amount.amount),
      discounts = listOf(discount),
    )
    return InAppPurchaseOffer(
      id = id,
      variant = variant,
      exposureDisplayName = exposureDisplayName,
      cost = cost,
      costBreakdown = costBreakdownOf(variant.displayName, cost),
      displayItems = emptyList(),
      deductible = null,
      insuranceAmount = insuranceAmount,
      usps = emptyList(),
      startDate = startDate,
    )
  }

  // endregion

  // region signing

  const val ALREADY_SIGNING_MESSAGE = "Signing is already in progress. Open the BankID app to continue."
  const val QUOTE_NO_LONGER_VALID_MESSAGE =
    "Something has changed since you got your price. Go back and get a new price."
  const val SIGNING_CANCELLED_MESSAGE = "The signing was cancelled in the BankID app."

  private const val SIGNING_ID = "e2a7c4f9-8b1d-4e36-a5c0-6d9f3b2e7a18"
  private const val AUTO_START_TOKEN = "7c40b5c9-fa74-49cf-b98c-bfe651f9a7c6"

  /**
   * Starts a new mocked signing, which [nextSigningPoll] then walks through PENDING → CREATING → SIGNED.
   */
  fun startSigning(): InAppPurchaseSigning {
    signingPollCount = 0
    return signing(InAppPurchaseSigningStatus.PENDING, qrSequence = 0, bankIdAppOpened = false)
  }

  fun nextSigningPoll(): InAppPurchaseSigning {
    signingPollCount++
    return when (signingPollCount) {
      1, 2 -> signing(InAppPurchaseSigningStatus.PENDING, qrSequence = signingPollCount * 3, bankIdAppOpened = true)
      3 -> signing(InAppPurchaseSigningStatus.CREATING, qrSequence = 12, bankIdAppOpened = true)
      else -> signing(InAppPurchaseSigningStatus.SIGNED, qrSequence = 12, bankIdAppOpened = true)
    }
  }

  val failedSigning: InAppPurchaseSigning
    get() = signing(InAppPurchaseSigningStatus.FAILED, qrSequence = 12, bankIdAppOpened = true)
      .copy(failureMessage = SIGNING_CANCELLED_MESSAGE)

  private fun signing(status: InAppPurchaseSigningStatus, qrSequence: Int, bankIdAppOpened: Boolean) =
    InAppPurchaseSigning(
      id = SIGNING_ID,
      status = status,
      autoStartToken = AUTO_START_TOKEN,
      liveQrCodeData = "bankid.67df3917-fa0d-44e5-b327-edcc928297f8.$qrSequence.mock",
      bankIdAppOpened = bankIdAppOpened,
      failureMessage = null,
    )

  // endregion

  // region product content

  private val accidentVariant = ProductVariant(
    displayName = "Accident Insurance",
    contractGroup = ContractGroup.ACCIDENT,
    contractType = ContractType.SE_ACCIDENT,
    partner = null,
    perils = listOf(
      ProductVariantPeril(
        id = "accident-medical-care",
        title = "Medical costs",
        description = "Compensation for medical care, treatment and travel in connection with an accident injury.",
        covered = listOf("Medical care", "Physiotherapy", "Travel to and from care"),
        colorCode = "#C0E4F3",
      ),
      ProductVariantPeril(
        id = "accident-dental",
        title = "Dental injuries",
        description = "Compensation for treating a dental injury caused by an accident.",
        covered = listOf("Akut tandbehandling", "Permanent tandbehandling"),
        colorCode = "#C0E4F3",
      ),
    ),
    insurableLimits = listOf(
      InsurableLimit("Medical disability", "1 000 000 kr", "Maximum compensation for a permanent injury."),
      InsurableLimit("Dental injuries", "Reasonable costs", "Treatment of a dental injury after an accident."),
    ),
    documents = listOf(
      document("Terms and conditions", "se-accident-terms.pdf", InsuranceDocumentType.TERMS_AND_CONDITIONS),
      document("Pre-sale information", "se-accident-pre-sale.pdf", InsuranceDocumentType.PRE_SALE_INFO),
    ),
    displayTierName = null,
    tierDescription = null,
    termsVersion = "SE_ACCIDENT_20250601",
  )

  private val accidentProduct = InAppPurchaseProduct(
    name = "SE_ACCIDENT",
    displayNameShort = "Accident",
    displayNameFull = "Accident Insurance",
    tagline = "Compensation if you get injured, around the clock",
    pillowImage = InAppPurchaseImage(
      src = "https://a.storyblok.com/f/165473/832x832/accident-pillow.png",
      alt = "Accident",
    ),
    featuredImage = null,
    variants = listOf(accidentVariant),
    faq = listOf(
      InAppPurchaseFaqItem(
        question = "Who does the insurance cover?",
        answerMarkdown = "It covers you and the **co-insured** you add.",
      ),
      InAppPurchaseFaqItem(
        question = "Does it cover me abroad?",
        answerMarkdown = "Yes, worldwide during the first 45 days of a trip.",
      ),
    ),
  )

  private val paymentProtectionVariant = ProductVariant(
    displayName = "Payment Protection",
    contractGroup = ContractGroup.PAYMENT_PROTECTION,
    contractType = ContractType.UNKNOWN,
    partner = null,
    perils = listOf(
      ProductVariantPeril(
        id = "ppi-unemployment",
        title = "Unemployment",
        description = "Monthly compensation if you become involuntarily unemployed.",
        covered = listOf("Redundancy"),
        colorCode = "#E5E0F7",
      ),
      ProductVariantPeril(
        id = "ppi-sickness",
        title = "Sick leave",
        description = "Monthly compensation if you go on full-time sick leave.",
        covered = listOf("Full-time sick leave longer than 30 days"),
        colorCode = "#E5E0F7",
      ),
    ),
    insurableLimits = listOf(
      InsurableLimit("Monthly compensation", "Up to 12 000 kr", "Depends on the level you choose."),
    ),
    documents = listOf(
      document("Terms and conditions", "se-payment-protection-terms.pdf", InsuranceDocumentType.TERMS_AND_CONDITIONS),
      document("Pre-sale information", "se-payment-protection-pre-sale.pdf", InsuranceDocumentType.PRE_SALE_INFO),
    ),
    displayTierName = null,
    tierDescription = null,
    termsVersion = "SE_PAYMENT_PROTECTION_LOW_20260401",
  )

  private val paymentProtectionProduct = InAppPurchaseProduct(
    name = "SE_PAYMENT_PROTECTION",
    displayNameShort = "Payment Protection",
    displayNameFull = "Payment Protection",
    tagline = "Help with your bills if you get sick or lose your job",
    pillowImage = InAppPurchaseImage(
      src = "https://a.storyblok.com/f/165473/832x832/payment-protection-pillow.png",
      alt = "Payment Protection",
    ),
    featuredImage = null,
    variants = listOf(paymentProtectionVariant),
    faq = listOf(
      InAppPurchaseFaqItem(
        question = "How long do I get compensation?",
        answerMarkdown = "Up to **12 months** per claim.",
      ),
      InAppPurchaseFaqItem(
        question = "Is there a qualifying period?",
        answerMarkdown = "Yes, 30 days from when the insurance starts.",
      ),
    ),
  )

  // endregion

  private fun document(displayName: String, fileName: String, type: InsuranceDocumentType) =
    InsuranceVariantDocument(displayName, "https://assets.hedvig.com/terms/$fileName", type)

  private fun option(value: String, title: String, isSelectable: Boolean = true, additionalInfo: String? = null) =
    InAppPurchaseFormField.SingleSelect.Option(value, title, isSelectable, additionalInfo)

  private fun sek(amount: Double) = UiMoney(amount, UiCurrencyCode.SEK)
}
