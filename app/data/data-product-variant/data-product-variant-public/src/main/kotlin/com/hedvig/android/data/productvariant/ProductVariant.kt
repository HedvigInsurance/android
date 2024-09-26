package com.hedvig.android.data.productvariant

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType

data class ProductVariant(
  val displayName: String,
  val contractGroup: ContractGroup,
  val contractType: ContractType,
  val partner: String?,
  val perils: List<ProductVariantPeril>,
  val insurableLimits: List<InsurableLimit>,
  val documents: List<InsuranceVariantDocument>,
  val tierName: String? = null, // todo: not sure if we will need it here
)

data class ProductVariantPeril(
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

data class InsuranceVariantDocument(
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
    CERTIFICATE,
    UNKNOWN__,
  }
}
