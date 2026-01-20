package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [delta] is the [Offset] of how far away the source coming from [underlyingInteractionSource] is, relevant to the
 * returned [MutableInteractionSource].
 */
class MappedInteractionSource(
  private val underlyingInteractionSource: MutableInteractionSource,
  private val delta: () -> Offset,
) : MutableInteractionSource {
  private val mappedPresses = mutableMapOf<PressInteraction.Press, PressInteraction.Press>()

  override val interactions: Flow<Interaction> = underlyingInteractionSource.interactions.map { interaction ->
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

  override suspend fun emit(interaction: Interaction) {
    underlyingInteractionSource.emit(interaction)
  }

  override fun tryEmit(interaction: Interaction): Boolean {
    return underlyingInteractionSource.tryEmit(interaction)
  }

  private fun mapPress(press: PressInteraction.Press): PressInteraction.Press {
    return PressInteraction.Press((press.pressPosition + delta()))
  }
}
