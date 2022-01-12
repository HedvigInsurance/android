package com.hedvig.app.util

import androidx.annotation.FloatRange
import androidx.compose.ui.unit.Dp

@JvmInline
value class ProgressPercentage(@FloatRange(from = 0.0, to = 1.0) val value: Float) {
    val isDone: Boolean
        get() = value == 1f

    companion object {
        fun of(
            current: Dp,
            target: Dp,
        ): ProgressPercentage {
            return ProgressPercentage(
                (current / target).coerceIn(0f, 1f)
            )
        }
    }
}
