package com.hedvig.app.util

import androidx.annotation.FloatRange

@JvmInline
value class ProgressPercentage(@FloatRange(from = 0.0, to = 1.0) val value: Float) {
    val isDone: Boolean
        get() = value == 1f
}
