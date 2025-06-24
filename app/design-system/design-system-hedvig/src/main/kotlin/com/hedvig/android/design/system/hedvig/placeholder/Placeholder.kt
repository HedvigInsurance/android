package com.hedvig.android.design.system.hedvig.placeholder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.ShapedColorPainter
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.contentColorFor
import com.hedvig.android.placeholder.PlaceholderDefaults
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.placeholder as corePlaceholder

@Composable
fun crossSellPainterFallback() = ShapedColorPainter(
  HedvigTheme.shapes.cornerXLarge,
  PlaceholderDefaults.color(),
)

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PlaceholderDefaults.color(
  backgroundColor: Color = HedvigTheme.colorScheme.surfacePrimary,
  contentColor: Color = contentColorFor(backgroundColor),
  contentAlpha: Float = 0.1f,
): Color = contentColor.copy(contentAlpha).compositeOver(backgroundColor)

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PlaceholderDefaults.fadeHighlightColor(
  backgroundColor: Color = HedvigTheme.colorScheme.surfacePrimary,
  alpha: Float = 0.3f,
): Color = backgroundColor.copy(alpha = alpha)

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PlaceholderDefaults.shimmerHighlightColor(
  backgroundColor: Color = HedvigTheme.colorScheme.fillNegative,
  alpha: Float = 0.75f,
): Color {
  return backgroundColor.copy(alpha = alpha)
}

fun Modifier.hedvigPlaceholder(
  visible: Boolean,
  shape: Shape,
  color: Color = Color.Unspecified,
  highlight: PlaceholderHighlight? = null,
  placeholderFadeAnimationSpec: AnimationSpec<Float> = spring(),
  contentFadeAnimationSpec: AnimationSpec<Float> = spring(),
): Modifier = this.composed {
  val themedColor = if (color.isSpecified) color else PlaceholderDefaults.color()
  Modifier.corePlaceholder(
    visible = visible,
    color = themedColor,
    shape = shape,
    highlight = highlight,
    placeholderFadeAnimationSpec = placeholderFadeAnimationSpec,
    contentFadeAnimationSpec = contentFadeAnimationSpec,
  )
}

@HedvigPreview
@Composable
private fun PreviewFade() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Box(
        Modifier.size(
          200.dp,
        ).hedvigPlaceholder(true, HedvigTheme.shapes.cornerMedium, highlight = PlaceholderHighlight.fade()),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewShimmer() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Box(
        Modifier.size(
          200.dp,
        ).hedvigPlaceholder(true, HedvigTheme.shapes.cornerMedium, highlight = PlaceholderHighlight.shimmer()),
      )
    }
  }
}
