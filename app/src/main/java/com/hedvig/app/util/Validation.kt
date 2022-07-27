package com.hedvig.app.util

import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import com.hedvig.app.R

object Regexes {
  val phoneNumberRegex = Regex("([+]*[0-9]+[+. -]*)")
}

fun String.isValidEmail() = isBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()

fun validateEmail(email: CharSequence): ValidationResult =
  if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
    ValidationResult(false, hedvig.resources.R.string.PROFILE_MY_INFO_INVALID_EMAIL)
  } else {
    ValidationResult(true, null)
  }

fun validatePhoneNumber(phoneNumber: CharSequence): ValidationResult =
  if (!Regexes.phoneNumberRegex.matches(phoneNumber)) {
    ValidationResult(false, hedvig.resources.R.string.PROFILE_MY_INFO_INVALID_PHONE_NUMBER)
  } else {
    ValidationResult(true, null)
  }

fun validateNationalIdentityNumber(nationalIdentityNumber: String): ValidationResult =
  if (nationalIdentityNumber.length > 11) {
    ValidationResult(false, hedvig.resources.R.string.INVALID_NATIONAL_IDENTITY_NUMBER)
  } else {
    ValidationResult(true, null)
  }

data class ValidationResult(val isSuccessful: Boolean, @StringRes val errorTextKey: Int?)
