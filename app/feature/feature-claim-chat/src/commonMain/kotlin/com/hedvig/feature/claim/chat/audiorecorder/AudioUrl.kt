package com.hedvig.feature.claim.chat.audiorecorder

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AudioUrl(val value: String)
