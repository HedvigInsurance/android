package com.hedvig.audio.player.data

import androidx.annotation.FloatRange

@JvmInline
value class ProgressPercentage(
  @FloatRange(from = 0.0, to = 1.0, fromInclusive = true, toInclusive = true)
  val value: Float,
) {
  init {
    require(value in 0.0f..1.0f) {
      "Progress percentage must be within 0.0f inclusive to 1.0f inclusive. Value: $value"
    }
  }

  val isDone: Boolean
    get() = value == 1f

  companion object {
    fun safeValue(float: Float): ProgressPercentage {
      if (float.isNaN()) return ProgressPercentage(0f)
      return ProgressPercentage(float.coerceIn(0f, 1f))
    }

    fun of(current: Float, target: Float): ProgressPercentage {
      return ProgressPercentage(
        (current / target).coerceIn(0f, 1f),
      )
    }
  }
}
