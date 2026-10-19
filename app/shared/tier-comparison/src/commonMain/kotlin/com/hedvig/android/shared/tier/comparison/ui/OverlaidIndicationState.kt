package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset

@Composable
internal fun rememberOverlaidIndicationState(): OverlaidIndicationState {
  return remember { OverlaidIndicationStateImpl() }
}

@Stable
internal interface OverlaidIndicationState {
  // Used to be able to offset the interactions coming from the cell rows by this much, so that they match the physical
  // position of the origin of the interaction
  var fixedColumnWidth: Int
  var offset: IntOffset
  val interactionSource: MutableInteractionSource
}

private class OverlaidIndicationStateImpl() : OverlaidIndicationState {
  override var fixedColumnWidth by mutableIntStateOf(0)
  override var offset by mutableStateOf(IntOffset(0, 0))
  override val interactionSource: MutableInteractionSource = MutableInteractionSource()
}
