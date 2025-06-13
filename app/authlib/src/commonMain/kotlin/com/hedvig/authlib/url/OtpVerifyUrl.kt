package com.hedvig.authlib.url

import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
internal value class OtpVerifyUrl(
  @SerialName("verifyUrl")
  internal val url: String,
)
