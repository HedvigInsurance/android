package com.hedvig.android.feature.forever.data

internal fun ForeverRepository.ReferralError?.toErrorMessage(): String? = when (this) {
  ForeverRepository.ReferralError.CodeExists -> "Code exists" // TODO string resources
  is ForeverRepository.ReferralError.CodeTooLong -> "Code too long"
  is ForeverRepository.ReferralError.CodeTooShort -> "Too short"
  is ForeverRepository.ReferralError.GeneralError -> "General Error"
  is ForeverRepository.ReferralError.MaxUpdates -> "Max updates"
  null -> null
}
