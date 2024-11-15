package com.hedvig.android.data.productVariant.android

import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.data.contract.toContractType
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import hedvig.resources.R
import octopus.fragment.ProductVariantFragment
import octopus.type.InsuranceDocumentType

fun InsuranceVariantDocument.InsuranceDocumentType.getStringRes() = when (this) {
  InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS -> R.string.MY_DOCUMENTS_INSURANCE_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS -> R.string.MY_DOCUMENTS_GENERAL_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRIVACY_POLICY -> R.string.MY_DOCUMENTS_PRIVACY_POLICY
  InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE -> R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE
  InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__ -> null
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
      exceptions = peril.exceptions,
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
      type = @Suppress("ktlint:standard:max-line-length")
      when (document.type) {
        InsuranceDocumentType.TERMS_AND_CONDITIONS -> InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS
        InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD
        InsuranceDocumentType.PRE_SALE_INFO -> InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO
        InsuranceDocumentType.GENERAL_TERMS -> InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS
        InsuranceDocumentType.PRIVACY_POLICY -> InsuranceVariantDocument.InsuranceDocumentType.PRIVACY_POLICY
        InsuranceDocumentType.UNKNOWN__ -> InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__
      },
    )
  },
)
