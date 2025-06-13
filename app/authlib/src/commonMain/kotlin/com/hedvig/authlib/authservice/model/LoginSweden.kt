package com.hedvig.authlib.authservice.model

import com.hedvig.authlib.url.LoginStatusUrl
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LoginSwedenInput(val personalNumber: String?) {
  @EncodeDefault
  val method: String = "SE_BANKID"
}

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("result")
@Serializable
internal sealed interface LoginSwedenResponse {
  @Serializable
  @SerialName("success")
  data class Success(
    val id: String,
    val statusUrl: LoginStatusUrl,
    val seBankIdProperties: SeBankIdProperties,
  ) : LoginSwedenResponse {
    @Serializable
    data class SeBankIdProperties(val autoStartToken: String)
  }

  @Serializable
  @SerialName("error")
  data class Error(val reason: String) : LoginSwedenResponse
}
