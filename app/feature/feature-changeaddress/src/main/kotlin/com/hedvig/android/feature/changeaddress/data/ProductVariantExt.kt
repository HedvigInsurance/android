package com.hedvig.android.feature.changeaddress.data

import octopus.type.InsuranceDocumentType

internal fun InsuranceDocumentType.documentDisplayName() = when (this) {
  InsuranceDocumentType.TERMS_AND_CONDITIONS -> hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_TERMS
  InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> hedvig.resources.R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceDocumentType.PRE_SALE_INFO -> hedvig.resources.R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
  InsuranceDocumentType.GENERAL_TERMS -> hedvig.resources.R.string.MY_DOCUMENTS_GENERAL_TERMS
  InsuranceDocumentType.PRIVACY_POLICY -> hedvig.resources.R.string.MY_DOCUMENTS_PRIVACY_POLICY
  InsuranceDocumentType.UNKNOWN__ -> null
}
