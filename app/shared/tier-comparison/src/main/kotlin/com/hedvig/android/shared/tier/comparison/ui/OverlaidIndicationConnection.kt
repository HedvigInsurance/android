package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun Modifier.overlaidIndicationConnection(
  overlaidIndicationState: OverlaidIndicationState,
  interactionSource: MutableInteractionSource,
): Modifier = this then OverlaidIndicationConnectionElement(overlaidIndicationState, interactionSource)

private data class OverlaidIndicationConnectionElement(
  val overlaidIndicationState: OverlaidIndicationState,
  val interactionSource: MutableInteractionSource,
) : ModifierNodeElement<OverlaidIndicationConnectionNode>() {
  override fun create(): OverlaidIndicationConnectionNode {
    return OverlaidIndicationConnectionNode(overlaidIndicationState, interactionSource)
  }

  override fun update(node: OverlaidIndicationConnectionNode) {
    node.update(overlaidIndicationState, interactionSource)
  }
}

private class OverlaidIndicationConnectionNode(
  private var overlaidIndicationState: OverlaidIndicationState,
  private var interactionSource: MutableInteractionSource,
) : LayoutAwareModifierNode, Modifier.Node() {
  private var offset = IntOffset(0, 0)
  private var collectingJob: Job? = null

  override fun onAttach() {
    startCollection()
  }

  fun update(overlaidIndicationState: OverlaidIndicationState, interactionSource: MutableInteractionSource) {
    this.overlaidIndicationState = overlaidIndicationState
    this.interactionSource = interactionSource
    startCollection()
  }

  private fun startCollection() {
    collectingJob?.cancel()
    collectingJob = coroutineScope.launch {
      interactionSource.interactions.collect {
        overlaidIndicationState.offset = offset
        overlaidIndicationState.interactionSource.tryEmit(it)
      }
    }
  }

  override fun onPlaced(coordinates: LayoutCoordinates) {
    offset = coordinates.positionInParent().round()
  }
}
