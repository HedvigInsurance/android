package com.hedvig.android.data.contract

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AddonId(val id: String)
