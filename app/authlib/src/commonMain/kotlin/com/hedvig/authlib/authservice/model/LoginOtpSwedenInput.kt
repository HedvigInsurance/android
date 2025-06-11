package com.hedvig.authlib.authservice.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LoginOtpSwedenInput(val email: String) {
  @EncodeDefault
  val method: String = "OTP"
}
