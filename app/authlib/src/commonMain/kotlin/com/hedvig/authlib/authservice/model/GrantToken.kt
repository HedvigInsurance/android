package com.hedvig.authlib.authservice.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("grant_type")
@Serializable
internal sealed interface GrantTokenInput {
  @Serializable
  @SerialName("authorization_code")
  data class AuthorizationCode(
    @SerialName("authorization_code")
    val authorizationCode: String,
  ) : GrantTokenInput

  @Serializable
  @SerialName("refresh_token")
  data class RefreshToken(
    @SerialName("refresh_token")
    val refreshToken: String,
  ) : GrantTokenInput
}

@Serializable
internal data class GrantTokenOutput(
  @SerialName("access_token")
  val accessToken: String,
  @SerialName("expires_in")
  val accessTokenExpiresIn: Long,
  @SerialName("refresh_token")
  val refreshToken: String,
  @SerialName("refresh_token_expires_in")
  val refreshTokenExpiresIn: Long,
)
