package com.hedvig.android.core.ui.layout

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * A [Placeable] to be used inside custom [androidx.compose.ui.layout.Layout] if we need to add some spacing manually.
 */
class FixedSizePlaceable(width: Int, height: Int) : Placeable() {
  init {
    measuredSize = IntSize(width, height)
  }

  override fun get(alignmentLine: AlignmentLine): Int = AlignmentLine.Unspecified

  override fun placeAt(position: IntOffset, zIndex: Float, layerBlock: (GraphicsLayerScope.() -> Unit)?) = Unit
}
