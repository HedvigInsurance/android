package com.hedvig.android.data.paying.member

import com.hedvig.android.core.common.validation.PhoneNumberRules

/**
 * [storedNumber] ready for a Swish field, or null when it cannot be read as a number the backend
 * takes, in which case there is nothing safe to prefill and the member types it.
 *
 * The backend accepts `07…`, `467…` and `+467…` and normalises them itself, so the stored form is
 * passed through rather than rewritten — `+46` is what Swish ends up wanting anyway. Only
 * separators are dropped, since they are formatting the field applies for itself.
 */
fun swishPhoneNumberOrNull(storedNumber: String): String? {
  // A letter is a mistake rather than formatting, so it makes the whole value unreadable instead of
  // being quietly dropped along with the separators.
  if (storedNumber.any { it.isLetter() }) return null
  val hasCountryCodePlus = storedNumber.trimStart().startsWith("+")
  val digits = storedNumber.filter { it.isDigit() }
  val lengthAccepted = digits.length >= PhoneNumberRules.SwishPhoneNumber.minDigits &&
    digits.length <= PhoneNumberRules.E164_MAX_DIGITS
  val prefixAccepted = when {
    digits.startsWith("467") -> true

    // A plus belongs to a country code, so it cannot lead a domestic number.
    digits.startsWith("07") -> !hasCountryCodePlus

    else -> false
  }
  if (!lengthAccepted || !prefixAccepted) return null
  return if (hasCountryCodePlus) "+$digits" else digits
}

/** A Swish number grouped the way the member is used to reading it: `070-123-45-67`. */
fun formatSwishPhoneNumber(phoneNumber: String): String {
  val digits = phoneNumber.take(15)
  val sb = StringBuilder()
  for (i in digits.indices) {
    sb.append(digits[i])
    if (i in setOf(2, 5, 7)) {
      sb.append("-")
    }
  }
  return sb.toString()
}
