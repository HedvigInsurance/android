package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * [delta] is the [Offset] of how far away the source coming from [underlyingInteractionSource] is, relevant to the
 * returned [MutableInteractionSource].
 *
 * An indication collects [interactions] only while it is attached. When its node is detached and attached again, as
 * when movable content moves it, the hover exit and unfocus emitted in between reach no collector, and the indication
 * would keep showing that hover or focus. So a collection first receives the end of every hover and focus that the
 * previous collection saw start and not end, and then the live interactions. Presses are not replayed: a press that
 * was in flight still ends through its release or cancel, mapped to the same mapped press. The tracking assumes one
 * collector at a time, the indication: concurrent collectors would each record the same hovers and focuses.
 */
class MappedInteractionSource(
  private val underlyingInteractionSource: MutableInteractionSource,
  private val delta: () -> Offset,
) : MutableInteractionSource {
  private val mappedPresses = mutableMapOf<PressInteraction.Press, PressInteraction.Press>()
  private val openHovers = mutableListOf<HoverInteraction.Enter>()
  private val openFocuses = mutableListOf<FocusInteraction.Focus>()

  override val interactions: Flow<Interaction> = flow {
    val missedEnds = openHovers.map(HoverInteraction::Exit) + openFocuses.map(FocusInteraction::Unfocus)
    openHovers.clear()
    openFocuses.clear()
    missedEnds.forEach { emit(it) }
    emitAll(underlyingInteractionSource.interactions.map(::track))
  }

  override suspend fun emit(interaction: Interaction) {
    underlyingInteractionSource.emit(interaction)
  }

  override fun tryEmit(interaction: Interaction): Boolean {
    return underlyingInteractionSource.tryEmit(interaction)
  }

  private fun track(interaction: Interaction): Interaction = when (interaction) {
    is HoverInteraction.Enter -> {
      openHovers += interaction
      interaction
    }

    is HoverInteraction.Exit -> {
      openHovers -= interaction.enter
      interaction
    }

    is FocusInteraction.Focus -> {
      openFocuses += interaction
      interaction
    }

    is FocusInteraction.Unfocus -> {
      openFocuses -= interaction.focus
      interaction
    }

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

    else -> {
      interaction
    }
  }

  private fun mapPress(press: PressInteraction.Press): PressInteraction.Press {
    return PressInteraction.Press((press.pressPosition + delta()))
  }
}
