package com.hedvig.android.placeholder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Contains default values used by [Modifier.placeholder] and [PlaceholderHighlight].
 */
object PlaceholderDefaults {
  /**
   * The default [InfiniteRepeatableSpec] to use for [fade].
   */
  val fadeAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
    infiniteRepeatable(
      animation = tween(delayMillis = 200, durationMillis = 600),
      repeatMode = RepeatMode.Reverse,
    )
  }

  /**
   * The default [InfiniteRepeatableSpec] to use for [shimmer].
   */
  val shimmerAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
    infiniteRepeatable(
      animation = tween(durationMillis = 1700, delayMillis = 200),
      repeatMode = RepeatMode.Restart,
    )
  }
}

fun Modifier.placeholder(
  visible: Boolean,
  color: Color,
  shape: Shape = RectangleShape,
  highlight: PlaceholderHighlight? = null,
  placeholderFadeAnimationSpec: AnimationSpec<Float> = spring(),
  contentFadeAnimationSpec: AnimationSpec<Float> = spring(),
): Modifier = this then PlaceholderElement(
  visible = visible,
  color = color,
  shape = shape,
  highlight = highlight,
  placeholderFadeAnimationSpec = placeholderFadeAnimationSpec,
  contentFadeAnimationSpec = contentFadeAnimationSpec,
)

private data class PlaceholderElement(
  private val visible: Boolean,
  private val color: Color,
  private val shape: Shape,
  private val highlight: PlaceholderHighlight?,
  private val placeholderFadeAnimationSpec: AnimationSpec<Float>,
  private val contentFadeAnimationSpec: AnimationSpec<Float>,
) : ModifierNodeElement<PlaceholderNode>() {
  override fun create(): PlaceholderNode = PlaceholderNode(
    visible = visible,
    color = color,
    shape = shape,
    highlight = highlight,
    placeholderFadeAnimationSpec = placeholderFadeAnimationSpec,
    contentFadeAnimationSpec = contentFadeAnimationSpec,
  )

  override fun update(node: PlaceholderNode) {
    node.apply {
      updateVisible(visible)
      updateColor(color)
      updateShape(shape)
      updateHighlight(highlight)
      updatePlaceholderFadeAnimationSpec(placeholderFadeAnimationSpec)
      updateContentFadeAnimationSpec(contentFadeAnimationSpec)
    }
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "placeholder"
    value = visible
    properties["visible"] = visible
    properties["color"] = color
    properties["highlight"] = highlight
    properties["shape"] = shape
  }
}

private class PlaceholderNode(
  private var visible: Boolean,
  private var color: Color,
  private var shape: Shape = RectangleShape,
  private var highlight: PlaceholderHighlight? = null,
  private var placeholderFadeAnimationSpec: AnimationSpec<Float>,
  private var contentFadeAnimationSpec: AnimationSpec<Float>,
) : DrawModifierNode, Modifier.Node() {
  private val crossfadeTransitionState = MutableTransitionState(visible).apply {
    targetState = visible
  }

  private val paint: Paint = Paint()

  private var contentAlpha: Float = if (visible) 0F else 1F
  private var placeholderAlpha: Float = if (visible) 1F else 0F

  // The current highlight animation progress
  private var highlightProgress: Float = 0F

  // Values used for caching purposes
  private var lastSize: Size = Size.Unspecified
  private var lastLayoutDirection: LayoutDirection? = null
  private var lastOutline: Outline? = null

  fun updateVisible(visible: Boolean) {
    if (this.visible != visible) {
      this.visible = visible
      crossfadeTransitionState.targetState = visible
      coroutineScope.runAlphaAnimations()
      coroutineScope.runHighlightAnimation()
    }
  }

  fun updateColor(color: Color) {
    if (this.color != color) {
      this.color = color
    }
  }

  fun updateShape(shape: Shape) {
    if (this.shape != shape) {
      this.shape = shape
    }
  }

  fun updateHighlight(highlight: PlaceholderHighlight?) {
    if (this.highlight != highlight) {
      this.highlight = highlight
      coroutineScope.runHighlightAnimation()
    }
  }

  fun updatePlaceholderFadeAnimationSpec(placeholderFadeAnimationSpec: AnimationSpec<Float>) {
    if (this.placeholderFadeAnimationSpec != placeholderFadeAnimationSpec) {
      this.placeholderFadeAnimationSpec = placeholderFadeAnimationSpec
    }
  }

  fun updateContentFadeAnimationSpec(contentFadeAnimationSpec: AnimationSpec<Float>) {
    if (this.contentFadeAnimationSpec != contentFadeAnimationSpec) {
      this.contentFadeAnimationSpec = contentFadeAnimationSpec
    }
  }

  override fun onAttach() {
    coroutineScope.runAlphaAnimations()
    coroutineScope.runHighlightAnimation()
  }

  private val placeholderAnimation = Animatable(placeholderAlpha)
  private val contentAnimation = Animatable(contentAlpha)

  private fun CoroutineScope.runAlphaAnimations() {
    launch {
      placeholderAnimation.animateTo(
        targetValue = if (visible) 1F else 0F,
        placeholderFadeAnimationSpec,
      ) {
        val placeholderAlphaWas0 = placeholderAlpha < 0.01F
        placeholderAlpha = value
        if (placeholderAlphaWas0 && placeholderAlpha >= 0.01F && !visible) {
          coroutineScope.runHighlightAnimation()
        }
        invalidateDraw()
      }
    }

    launch {
      contentAnimation.animateTo(
        targetValue = if (visible) 0F else 1F,
        contentFadeAnimationSpec,
      ) {
        contentAlpha = value
        invalidateDraw()
      }
    }
  }

  private val infiniteAnimation = Animatable(0F)

  private fun CoroutineScope.runHighlightAnimation() {
    val isEffectivelyVisible = visible || placeholderAlpha >= 0.01F
    val animationSpec = highlight?.animationSpec
    if (isEffectivelyVisible && animationSpec != null) {
      launch {
        infiniteAnimation.animateTo(1F, animationSpec) {
          highlightProgress = value
          invalidateDraw()
        }
      }
    }
  }

  override fun ContentDrawScope.draw() {
    val drawContent = ::drawContent

    // Draw the composable content first
    if (contentAlpha in 0.01F..0.99F) {
      // If the content alpha is between 1% and 99%, draw it in a layer with
      // the alpha applied
      paint.alpha = contentAlpha
      withLayer(paint) {
        drawContent()
      }
    } else if (contentAlpha >= 0.99F) {
      // If the content alpha is > 99%, draw it with no alpha
      drawContent()
    }

    if (placeholderAlpha in 0.01F..0.99F) {
      // If the placeholder alpha is between 1% and 99%, draw it in a layer with
      // the alpha applied
      paint.alpha = placeholderAlpha
      withLayer(paint) {
        lastOutline = drawPlaceholder(
          shape = shape,
          color = color,
          highlight = highlight,
          progress = highlightProgress,
          lastOutline = lastOutline,
          lastLayoutDirection = lastLayoutDirection,
          lastSize = lastSize,
        )
      }
    } else if (placeholderAlpha >= 0.99F) {
      // If the placeholder alpha is > 99%, draw it with no alpha
      lastOutline = drawPlaceholder(
        shape = shape,
        color = color,
        highlight = highlight,
        progress = highlightProgress,
        lastOutline = lastOutline,
        lastLayoutDirection = lastLayoutDirection,
        lastSize = lastSize,
      )
    }

    // Keep track of the last size & layout direction
    lastSize = size
    lastLayoutDirection = layoutDirection
  }
}

private fun DrawScope.drawPlaceholder(
  shape: Shape,
  color: Color,
  highlight: PlaceholderHighlight?,
  progress: Float,
  lastOutline: Outline?,
  lastLayoutDirection: LayoutDirection?,
  lastSize: Size?,
): Outline? {
  // shortcut to avoid Outline calculation and allocation
  if (shape === RectangleShape) {
    // Draw the initial background color
    drawRect(color = color)

    if (highlight != null) {
      drawRect(
        brush = highlight.brush(progress, size),
        alpha = highlight.alpha(progress),
      )
    }
    // We didn't create an outline so return null
    return null
  }

  // Otherwise we need to create an outline from the shape
  val outline = lastOutline.takeIf {
    size == lastSize && layoutDirection == lastLayoutDirection
  } ?: shape.createOutline(size, layoutDirection, this)

  // Draw the placeholder color
  drawOutline(outline = outline, color = color)

  if (highlight != null) {
    drawOutline(
      outline = outline,
      brush = highlight.brush(progress, size),
      alpha = highlight.alpha(progress),
    )
  }

  // Return the outline we used
  return outline
}

private inline fun DrawScope.withLayer(paint: Paint, drawBlock: DrawScope.() -> Unit) = drawIntoCanvas { canvas ->
  canvas.saveLayer(size.toRect(), paint)
  drawBlock()
  canvas.restore()
}
