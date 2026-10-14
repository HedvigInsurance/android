package com.hedvig.android.feature.inapppurchase.data

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.feature.inapppurchase.navigation.InAppProduct
import kotlinx.datetime.LocalDate

internal const val MockNetworkDelay = 600L

/**
 * The backend's sample responses for the draft `inAppPurchase*` API, returned by the use cases until the API
 * exists. Also used as preview and test data.
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
    title = "Startdatum",
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
          title = "Hur många fler vill du försäkra?",
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
          title = "Vad har du för anställning?",
          options = listOf(
            option("PERMANENT_EMPLOYED_FULLTIME", "Fast anställd, heltid"),
            option("PERMANENT_EMPLOYED_PARTTIME", "Fast anställd, deltid"),
            option(
              value = "CONTRACT_EMPLOYED",
              title = "Visstidsanställd",
              additionalInfo = "Gäller vid sjukskrivning. Arbetslöshet täcks bara om du har varit anställd i minst " +
                "12 månader.",
            ),
            option("SELF_EMPLOYED", "Egenföretagare"),
            option(
              value = "UNEMPLOYED",
              title = "Arbetslös",
              isSelectable = false,
              additionalInfo = "Betalskyddet kräver att du arbetar när försäkringen börjar gälla.",
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
    "Vi hittade ingen folkbokföringsadress för dig. Kontakta oss så hjälper vi dig."

  fun quote(product: InAppProduct, fields: List<InAppPurchaseFieldInput>): InAppPurchaseQuote {
    val values = fields.associate { it.fieldId to it.values.firstOrNull() }
    val startDate = values[FIELD_START_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val coInsured = values[FIELD_CO_INSURED]?.toIntOrNull()
    val errors = buildList {
      if (startDate == null || startDate < today || startDate > maxStartDate) {
        add(InAppPurchaseFieldError(FIELD_START_DATE, "Välj ett datum från och med idag."))
      }
      when (product) {
        InAppProduct.ACCIDENT -> {
          if (coInsured == null || coInsured !in 0..5) {
            add(InAppPurchaseFieldError(FIELD_CO_INSURED, "Välj mellan 0 och 5 medförsäkrade."))
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
                "Betalskyddet kräver att du arbetar när försäkringen börjar gälla.",
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
          exposureDisplayName = if (numberCoInsured == 0) "Du" else "Du + $numberCoInsured",
          gross = gross,
          discount = InAppPurchaseDiscount("50% rabatt", sek(-gross / 2)),
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
          exposureDisplayName = "6 000 kr/mån",
          gross = 199.0,
          discount = InAppPurchaseDiscount("Samlingsrabatt", sek(-30.0)),
          insuranceAmount = InAppPurchaseInsuranceAmount("Ersättning per månad", 6000),
          startDate = startDate,
        ),
        offer(
          id = "f71b3c95-6e2a-4d08-8b4f-3a9d6c2e1b57",
          variant = paymentProtectionVariant.copy(
            displayTierName = "12 000 kr",
            termsVersion = "SE_PAYMENT_PROTECTION_HIGH_20260401",
          ),
          exposureDisplayName = "12 000 kr/mån",
          gross = 349.0,
          discount = InAppPurchaseDiscount("Samlingsrabatt", sek(-52.0)),
          insuranceAmount = InAppPurchaseInsuranceAmount("Ersättning per månad", 12000),
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

  const val ALREADY_SIGNING_MESSAGE = "Signering pågår redan. Öppna BankID-appen för att fortsätta."
  const val QUOTE_NO_LONGER_VALID_MESSAGE =
    "Något har ändrats sedan du fick ditt pris. Gå tillbaka och hämta ett nytt pris."
  const val SIGNING_CANCELLED_MESSAGE = "Signeringen avbröts i BankID-appen."

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
    displayName = "Olycksfallsförsäkring",
    contractGroup = ContractGroup.ACCIDENT,
    contractType = ContractType.SE_ACCIDENT,
    partner = null,
    perils = listOf(
      ProductVariantPeril(
        id = "accident-medical-care",
        title = "Vårdkostnader",
        description = "Ersättning för läkarvård, behandling och resor i samband med olycksfallsskada.",
        covered = listOf("Läkarvård", "Sjukgymnastik", "Resor till och från vård"),
        colorCode = "#C0E4F3",
      ),
      ProductVariantPeril(
        id = "accident-dental",
        title = "Tandskador",
        description = "Ersättning för behandling av tandskada som uppstått genom olycksfall.",
        covered = listOf("Akut tandbehandling", "Permanent tandbehandling"),
        colorCode = "#C0E4F3",
      ),
    ),
    insurableLimits = listOf(
      InsurableLimit("Medicinsk invaliditet", "1 000 000 kr", "Högsta ersättning vid bestående skada."),
      InsurableLimit("Tandskador", "Skäliga kostnader", "Behandling av tandskada efter olycksfall."),
    ),
    documents = listOf(
      document("Försäkringsvillkor", "se-accident-terms.pdf", InsuranceDocumentType.TERMS_AND_CONDITIONS),
      document("Förköpsinformation", "se-accident-pre-sale.pdf", InsuranceDocumentType.PRE_SALE_INFO),
    ),
    displayTierName = null,
    tierDescription = null,
    termsVersion = "SE_ACCIDENT_20250601",
  )

  private val accidentProduct = InAppPurchaseProduct(
    name = "SE_ACCIDENT",
    displayNameShort = "Olycksfall",
    displayNameFull = "Olycksfallsförsäkring",
    tagline = "Ersättning om du skadar dig, dygnet runt",
    pillowImage = InAppPurchaseImage(
      src = "https://a.storyblok.com/f/165473/832x832/accident-pillow.png",
      alt = "Olycksfall",
    ),
    featuredImage = null,
    variants = listOf(accidentVariant),
    faq = listOf(
      InAppPurchaseFaqItem(
        question = "Vem gäller försäkringen för?",
        answerMarkdown = "Den gäller för dig och de **medförsäkrade** du lägger till.",
      ),
      InAppPurchaseFaqItem(
        question = "Gäller den utomlands?",
        answerMarkdown = "Ja, i hela världen under de första 45 dagarna av en resa.",
      ),
    ),
  )

  private val paymentProtectionVariant = ProductVariant(
    displayName = "Betalskydd",
    contractGroup = ContractGroup.PAYMENT_PROTECTION,
    contractType = ContractType.UNKNOWN,
    partner = null,
    perils = listOf(
      ProductVariantPeril(
        id = "ppi-unemployment",
        title = "Arbetslöshet",
        description = "Månatlig ersättning om du blir ofrivilligt arbetslös.",
        covered = listOf("Uppsägning på grund av arbetsbrist"),
        colorCode = "#E5E0F7",
      ),
      ProductVariantPeril(
        id = "ppi-sickness",
        title = "Sjukskrivning",
        description = "Månatlig ersättning om du blir helt sjukskriven.",
        covered = listOf("Hel sjukskrivning längre än 30 dagar"),
        colorCode = "#E5E0F7",
      ),
    ),
    insurableLimits = listOf(
      InsurableLimit("Ersättning per månad", "Upp till 12 000 kr", "Beror på vilken nivå du väljer."),
    ),
    documents = listOf(
      document("Försäkringsvillkor", "se-payment-protection-terms.pdf", InsuranceDocumentType.TERMS_AND_CONDITIONS),
      document("Förköpsinformation", "se-payment-protection-pre-sale.pdf", InsuranceDocumentType.PRE_SALE_INFO),
    ),
    displayTierName = null,
    tierDescription = null,
    termsVersion = "SE_PAYMENT_PROTECTION_LOW_20260401",
  )

  private val paymentProtectionProduct = InAppPurchaseProduct(
    name = "SE_PAYMENT_PROTECTION",
    displayNameShort = "Betalskydd",
    displayNameFull = "Betalskydd",
    tagline = "Hjälp med räkningarna om du blir sjuk eller arbetslös",
    pillowImage = InAppPurchaseImage(
      src = "https://a.storyblok.com/f/165473/832x832/payment-protection-pillow.png",
      alt = "Betalskydd",
    ),
    featuredImage = null,
    variants = listOf(paymentProtectionVariant),
    faq = listOf(
      InAppPurchaseFaqItem(
        question = "Hur länge får jag ersättning?",
        answerMarkdown = "Upp till **12 månader** per skadetillfälle.",
      ),
      InAppPurchaseFaqItem(
        question = "Finns det en karenstid?",
        answerMarkdown = "Ja, 30 dagar från att försäkringen börjar gälla.",
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
