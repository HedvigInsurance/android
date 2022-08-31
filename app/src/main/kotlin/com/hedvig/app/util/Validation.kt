package com.hedvig.app.util

import com.hedvig.android.core.common.validation.ValidationResult
import hedvig.resources.R

object Regexes {
  val phoneNumberRegex = Regex("([+]*[0-9]+[+. -]*)")
}

fun validatePhoneNumber(phoneNumber: CharSequence): ValidationResult =
  if (!Regexes.phoneNumberRegex.matches(phoneNumber)) {
    ValidationResult(false, R.string.PROFILE_MY_INFO_INVALID_PHONE_NUMBER)
  } else {
    ValidationResult(true, null)
  }

fun validateNationalIdentityNumber(nationalIdentityNumber: String): ValidationResult =
  if (nationalIdentityNumber.length > 11) {
    ValidationResult(false, R.string.INVALID_NATIONAL_IDENTITY_NUMBER)
  } else {
    ValidationResult(true, null)
  }
