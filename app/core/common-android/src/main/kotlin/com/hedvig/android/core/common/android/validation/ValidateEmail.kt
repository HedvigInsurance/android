package com.hedvig.android.core.common.android.validation

import androidx.core.util.PatternsCompat
import hedvig.resources.R

fun validateEmail(email: CharSequence): ValidationResult {
  return if (!isValidEmail(email.toString())) {
    ValidationResult(false, R.string.PROFILE_MY_INFO_INVALID_EMAIL)
  } else {
    ValidationResult(true, null)
  }
}

private fun isValidEmail(email: String): Boolean {
  return email.isNotBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
}
