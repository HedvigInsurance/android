package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Fills [shape] with the iOS-style glass material: a translucent fill that lets the backdrop through, a
 * drop shadow, rim shading, and a hairline highlight along the top edge.
 *
 * Draws behind the content it is applied to, so put it before any `clickable` that should ripple on top
 * of it.
 */
@Composable
fun Modifier.liquidGlass(shape: Shape): Modifier =
  glassMaterial(regularGlassMaterial, HedvigTheme.colorScheme.surfaceLiquidGlass, shape)

/**
 * The rim and shadow treatment of the glass material: a [dropShadow] behind the container, two inner
 * shadows over its fill that shade the top-left rim and light up the bottom-right one, and an optional
 * hairline [rimHighlight] where the material catches the light along its top edge.
 */
@Immutable
internal data class GlassMaterial(
  val dropShadow: Shadow,
  val rimShade: Shadow,
  val rimSheen: Shadow,
  val rimHighlight: BorderStroke?,
)

/** Paints [material] over a [fill] container, both clipped to [shape]. */
internal fun Modifier.glassMaterial(material: GlassMaterial, fill: Color, shape: Shape): Modifier = this
  .dropShadow(shape, material.dropShadow)
  .background(fill, shape)
  .innerShadow(shape, material.rimShade)
  .innerShadow(shape, material.rimSheen)
  .then(material.rimHighlight?.let { Modifier.border(it, shape) } ?: Modifier)

private val glassDropShadow = Shadow(
  radius = 40.dp,
  color = Color.Black,
  offset = DpOffset(0.dp, 8.dp),
  alpha = 0.12f,
)

// TODO: the rim shadows below stand in for a Figma glass effect that cannot be exported, so their
//  values are read off the Figma render rather than given by design. Revisit once design catches up.

/** The rim as the glass material normally reads: a faint shade and a pronounced sheen. */
internal val regularGlassMaterial = GlassMaterial(
  dropShadow = glassDropShadow,
  rimShade = Shadow(radius = 10.dp, color = Color.Black, offset = DpOffset(4.dp, 4.dp), alpha = 0.06f),
  rimSheen = Shadow(radius = 10.dp, color = Color.White, offset = DpOffset((-4).dp, (-4).dp), alpha = 0.80f),
  // Near-opaque along the top edge and down the upper half of the sides, gone by the bottom, where the
  // broader rimSheen takes over.
  rimHighlight = BorderStroke(
    width = 1.dp,
    brush = Brush.verticalGradient(
      0f to Color.White.copy(alpha = 0.85f),
      0.6f to Color.White.copy(alpha = 0.70f),
      1f to Color.White.copy(alpha = 0f),
    ),
  ),
)

/** The rim weighted for an opaque near-black fill, which would read as a grey smudge under [regularGlassMaterial]. */
internal val opaqueDarkGlassMaterial = GlassMaterial(
  dropShadow = glassDropShadow,
  rimShade = Shadow(radius = 10.dp, color = Color.Black, offset = DpOffset(4.dp, 4.dp), alpha = 0.40f),
  rimSheen = Shadow(radius = 10.dp, color = Color.White, offset = DpOffset((-4).dp, (-4).dp), alpha = 0.04f),
  rimHighlight = null,
)
