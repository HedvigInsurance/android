package com.hedvig.authlib.authservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginStatusResponse(
  val status: LoginStatus,
  val statusText: String,
  val authorizationCode: String?,
  val seBankIdProperties: BankIdProperties?,
) {
  @Serializable
  data class BankIdProperties(
    val autoStartToken: String,
    val liveQrCodeData: String,
    @SerialName("bankidAppOpened")
    val bankIdAppOpened: Boolean,
  )

  enum class LoginStatus {
    PENDING,
    FAILED,
    COMPLETED,
  }
}
