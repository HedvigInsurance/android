package com.hedvig.android.data.productvariant.android

import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import hedvig.resources.R

fun InsuranceVariantDocument.InsuranceDocumentType.getStringRes() = when (this) {
  InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS -> R.string.MY_DOCUMENTS_INSURANCE_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS -> R.string.MY_DOCUMENTS_GENERAL_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRIVACY_POLICY -> R.string.MY_DOCUMENTS_PRIVACY_POLICY
  InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE -> R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE
  InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__ -> null
}
