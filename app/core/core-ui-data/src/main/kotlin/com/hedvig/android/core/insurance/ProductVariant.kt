package com.hedvig.android.core.insurance

data class ProductVariant(
  val displayName: String,
  val typeOfContract: String,
  val partner: String?,
  val product: Product,
  val perils: List<Peril>,
  val insurableLimits: List<InsurableLimit>,
  val documents: List<Document>,
)

data class Product(
  val displayNameFull: String,
  val pillowImageUrl: String,
)

data class Peril(
  val id: String,
  val title: String,
  val description: String,
  val info: String,
  val covered: List<String>,
  val exceptions: List<String>,
  val colorCode: String?,
)

data class InsurableLimit(
  val label: String,
  val limit: String,
  val description: String,
  val type: InsurableLimitType,
) {
  enum class InsurableLimitType {
    DEDUCTIBLE,
    DEDUCTIBLE_NATURE_DAMAGE,
    DEDUCTIBLE_ALL_RISK,
    INSURED_AMOUNT,
    GOODS_INDIVIDUAL,
    GOODS_FAMILY,
    TRAVEL_DAYS,
    MEDICAL_EXPENSES,
    LOST_LUGGAGE,
    BIKE,
    PERMANENT_INJURY,
    TREATMENT,
    DENTAL_TREATMENT,
    TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME,
    TRAVEL_DELAYED_ON_TRIP,
    TRAVEL_DELAYED_LUGGAGE,
    TRAVEL_CANCELLATION,
    UNKNOWN,
  }
}

data class Document(
  val displayName: String,
  val url: String,
  val type: InsuranceDocumentType,
) {
  enum class InsuranceDocumentType {
    TERMS_AND_CONDITIONS,
    PRE_SALE_INFO_EU_STANDARD,
    PRE_SALE_INFO,
    GENERAL_TERMS,
    PRIVACY_POLICY,
    UNKNOWN__,
    ;

    fun getStringRes() = when (this) {
      TERMS_AND_CONDITIONS -> hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_TERMS
      PRE_SALE_INFO_EU_STANDARD -> hedvig.resources.R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
      PRE_SALE_INFO -> hedvig.resources.R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
      GENERAL_TERMS -> hedvig.resources.R.string.MY_DOCUMENTS_GENERAL_TERMS
      PRIVACY_POLICY -> hedvig.resources.R.string.MY_DOCUMENTS_PRIVACY_POLICY
      UNKNOWN__ -> null
    }
  }
}
