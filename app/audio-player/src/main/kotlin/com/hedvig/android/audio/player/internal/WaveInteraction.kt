package com.hedvig.android.audio.player.internal

import com.hedvig.android.core.common.android.ProgressPercentage

internal fun interface WaveInteraction {
  /**
   * [horizontalProgressPercentage] is a value that shows where in the horizontal spectrum the wave was interacted
   * with.
   * Ranges from 0.0f when interacted on the far left to 1.0f on the far right.
   */
  fun onInteraction(horizontalProgressPercentage: ProgressPercentage)
}
