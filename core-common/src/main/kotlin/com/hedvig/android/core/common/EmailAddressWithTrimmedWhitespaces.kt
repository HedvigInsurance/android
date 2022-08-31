package com.hedvig.android.core.common

import com.hedvig.android.core.common.validation.validateEmail

@JvmInline
value class EmailAddressWithTrimmedWhitespaces private constructor(val value: String) {
  val isValid: Boolean
    get() = validateEmail(value).isSuccessful

  companion object {
    operator fun invoke(email: String): EmailAddressWithTrimmedWhitespaces {
      return EmailAddressWithTrimmedWhitespaces(email.trim())
    }
  }
}
