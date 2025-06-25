package com.hedvig.authlib.authservice.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LoginOtpInput(
  val country: OtpLoginCountry,
  val personalNumber: String,
) {
  @EncodeDefault
  val method: String = "OTP"

  enum class OtpLoginCountry {
    NO,
    DK,
  }
}
