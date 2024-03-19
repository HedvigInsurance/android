package com.hedvig.android.core.designsystem.hedvig

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.map

/**
 * Adapts an [InteractionSource] from one component to another by mapping any interactions by a
 * given offset. Namely used for the pill indicator in [HedvigNavigationBarItem].
 */
internal class MappedInteractionSource(
  underlyingInteractionSource: InteractionSource,
  private val delta: Offset,
) : InteractionSource {
  private val mappedPresses =
    mutableMapOf<PressInteraction.Press, PressInteraction.Press>()

  override val interactions = underlyingInteractionSource.interactions.map { interaction ->
    when (interaction) {
      is PressInteraction.Press -> {
        val mappedPress = mapPress(interaction)
        mappedPresses[interaction] = mappedPress
        mappedPress
      }
      is PressInteraction.Cancel -> {
        val mappedPress = mappedPresses.remove(interaction.press)
        if (mappedPress == null) {
          interaction
        } else {
          PressInteraction.Cancel(mappedPress)
        }
      }
      is PressInteraction.Release -> {
        val mappedPress = mappedPresses.remove(interaction.press)
        if (mappedPress == null) {
          interaction
        } else {
          PressInteraction.Release(mappedPress)
        }
      }
      else -> interaction
    }
  }

  private fun mapPress(press: PressInteraction.Press): PressInteraction.Press =
    PressInteraction.Press(press.pressPosition - delta)
}
