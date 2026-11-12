package com.hedvig.core.common.android.validation

private val emailAddressRegex = Regex(EMAIL_ADDRESS_PATTERN)

actual fun isValidEmail(email: String): Boolean {
  return email.isNotBlank() && emailAddressRegex.matches(email)
}
