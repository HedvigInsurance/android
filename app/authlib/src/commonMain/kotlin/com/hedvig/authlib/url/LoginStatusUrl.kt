package com.hedvig.authlib.url

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
internal value class LoginStatusUrl(internal val url: String)
