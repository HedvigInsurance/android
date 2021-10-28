package com.hedvig.app.util.compose

import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable

enum class ContentAlpha {
    HIGH,
    MEDIUM,
    DISABLED,
    ;

    @Composable
    fun toComposableAlpha(): Float = when (this) {
        HIGH -> ContentAlpha.high
        MEDIUM -> ContentAlpha.medium
        DISABLED -> ContentAlpha.disabled
    }
}
