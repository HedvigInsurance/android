package com.hedvig.android.data.productvariant

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.data.contract.toContractType
import kotlinx.serialization.Serializable
import octopus.fragment.ProductVariantFragment
import octopus.type.InsuranceDocumentType

@Serializable
data class ProductVariant(
  val displayName: String,
  val contractGroup: ContractGroup,
  val contractType: ContractType,
  val partner: String?,
  val perils: List<ProductVariantPeril>,
  val insurableLimits: List<InsurableLimit>,
  val documents: List<InsuranceVariantDocument>,
  val displayTierName: String?,
  val tierDescription: String?,
  val termsVersion: String,
)

@Serializable
data class ProductVariantPeril(
  val id: String,
  val title: String,
  val description: String?,
  val covered: List<String>,
  val colorCode: String?,
)

@Serializable
data class InsurableLimit(
  val label: String,
  val limit: String,
  val description: String,
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

@Serializable
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

fun ProductVariantFragment.toProductVariant() = ProductVariant(
  tierDescription = tierDescription,
  displayTierName = displayNameTier,
  termsVersion = termsVersion,
  displayName = this.displayName,
  contractGroup = this.typeOfContract.toContractGroup(),
  contractType = this.typeOfContract.toContractType(),
  partner = this.partner,
  perils = this.perils.map { peril ->
    ProductVariantPeril(
      id = peril.id,
      title = peril.title,
      description = peril.description,
      covered = peril.covered,
      colorCode = peril.colorCode,
    )
  },
  insurableLimits = this.insurableLimits.map { insurableLimit ->
    InsurableLimit(
      label = insurableLimit.label,
      limit = insurableLimit.limit,
      description = insurableLimit.description,
    )
  },
  documents = this.documents.map { document ->
    InsuranceVariantDocument(
      displayName = document.displayName,
      url = document.url,
      type = document.type.toDocumentType(),
    )
  },
)

@Suppress("ktlint:standard:max-line-length")
fun InsuranceDocumentType.toDocumentType(): InsuranceVariantDocument.InsuranceDocumentType {
  return when (this) {
    InsuranceDocumentType.TERMS_AND_CONDITIONS -> InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS
    InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD
    InsuranceDocumentType.PRE_SALE_INFO -> InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO
    InsuranceDocumentType.GENERAL_TERMS -> InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS
    InsuranceDocumentType.PRIVACY_POLICY -> InsuranceVariantDocument.InsuranceDocumentType.PRIVACY_POLICY
    InsuranceDocumentType.UNKNOWN__ -> InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__
    InsuranceDocumentType.SCAR_TABLE -> InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__
  }
}
