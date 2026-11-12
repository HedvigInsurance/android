package com.hedvig.android.feature.login.genericauth

import com.hedvig.core.common.android.validation.isValidEmail

@JvmInline
value class EmailAddressWithTrimmedWhitespaces private constructor(val value: String) {
  val isValid: Boolean
    get() = isValidEmail(value)

  companion object {
    operator fun invoke(email: String): EmailAddressWithTrimmedWhitespaces {
      return EmailAddressWithTrimmedWhitespaces(email.trim())
    }
  }
}
