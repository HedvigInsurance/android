package com.hedvig.android.core.common.validation

import androidx.core.util.PatternsCompat

fun validateEmail(email: CharSequence): ValidationResult {
  return if (!isValidEmail(email.toString())) {
    ValidationResult(false, hedvig.resources.R.string.PROFILE_MY_INFO_INVALID_EMAIL)
  } else {
    ValidationResult(true, null)
  }
}

private fun isValidEmail(email: String): Boolean {
  return email.isNotBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
}
