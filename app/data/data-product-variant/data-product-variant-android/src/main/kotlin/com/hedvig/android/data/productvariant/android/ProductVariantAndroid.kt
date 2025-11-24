package com.hedvig.android.data.productvariant.android

import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import hedvig.resources.Res
import hedvig.resources.MY_DOCUMENTS_GENERAL_TERMS
import hedvig.resources.MY_DOCUMENTS_INSURANCE_CERTIFICATE
import hedvig.resources.MY_DOCUMENTS_INSURANCE_TERMS
import hedvig.resources.MY_DOCUMENTS_PRIVACY_POLICY
import hedvig.resources.MY_DOUMENTS_INSURANCE_PREPURCHASE

fun InsuranceVariantDocument.InsuranceDocumentType.getStringRes() = when (this) {
  InsuranceVariantDocument.InsuranceDocumentType.TERMS_AND_CONDITIONS -> Res.string.MY_DOCUMENTS_INSURANCE_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> Res.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO -> Res.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceVariantDocument.InsuranceDocumentType.GENERAL_TERMS -> Res.string.MY_DOCUMENTS_GENERAL_TERMS
  InsuranceVariantDocument.InsuranceDocumentType.PRIVACY_POLICY -> Res.string.MY_DOCUMENTS_PRIVACY_POLICY
  InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE -> Res.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE
  InsuranceVariantDocument.InsuranceDocumentType.UNKNOWN__ -> null
}
