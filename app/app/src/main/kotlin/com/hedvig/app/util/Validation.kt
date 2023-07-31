package com.hedvig.app.util

import com.hedvig.android.core.common.android.validation.ValidationResult
import hedvig.resources.R

fun validateNationalIdentityNumber(nationalIdentityNumber: String): ValidationResult =
  if (nationalIdentityNumber.length > 11) {
    ValidationResult(false, R.string.INVALID_NATIONAL_IDENTITY_NUMBER)
  } else {
    ValidationResult(true, null)
  }
