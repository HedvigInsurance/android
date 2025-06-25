package com.hedvig.authlib.authservice.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
internal data class OtpVerifyInput(val otp: String)

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("result")
@Serializable
internal sealed interface OtpVerifyResponse {
  @Serializable
  @SerialName("success")
  data class Success(val authorizationCode: String) : OtpVerifyResponse

  @Serializable
  @SerialName("error")
  data class Error(val statusText: String) : OtpVerifyResponse
}
