package com.hedvig.android.feature.purchase.common.data

data class SigningStart(
  val signingId: String,
  val autoStartToken: String,
)

data class SigningPollResult(
  val status: SigningStatus,
  val liveQrCodeData: String?,
)

enum class SigningStatus {
  PENDING,
  SIGNED,
  FAILED,
}
