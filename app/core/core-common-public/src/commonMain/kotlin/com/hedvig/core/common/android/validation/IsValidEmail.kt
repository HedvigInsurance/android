package com.hedvig.core.common.android.validation

expect fun isValidEmail(email: String): Boolean

/**
 * The pattern of `androidx.core.util.PatternsCompat.EMAIL_ADDRESS`, so targets without androidx.core
 * accept exactly the same addresses as Android.
 */
internal const val EMAIL_ADDRESS_PATTERN: String =
  "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
    "\\@" +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
    "(" +
    "\\." +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
    ")+"
