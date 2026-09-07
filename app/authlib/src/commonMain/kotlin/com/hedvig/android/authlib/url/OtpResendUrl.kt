package com.hedvig.android.authlib.url

import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
internal value class OtpResendUrl(
  @SerialName("resendUrl")
  internal val url: String,
)
