package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.tokens.CircularProgressIndicatorTokens.LinearEasing

/**
 * This modifier is to be used with FigmaShapes if the color for border is translucent,
 * otherwise the color will turn out wrong due to Modifier.border inner bug
 */
fun Modifier.borderForTranslucentColor(width: Dp, color: Color, shape: Shape) =
  this.border(width = width, brush = Brush.linearGradient(listOf(color, color)), shape = shape)

/**
 * Modifier to be added on the same screen as a TextField composable if we wish to be able to clear the focus of the
 * TextField without having to rely on using the IME action as our only option.
 */
fun Modifier.clearFocusOnTap(): Modifier = this.composed {
  val focusManager = LocalFocusManager.current
  Modifier.pointerInput(Unit) {
    detectTapGestures(
      onTap = { focusManager.clearFocus() },
    )
  }
}

/**
 * Used when quickly prototyping something and want to see how it renders
 */
@Suppress("unused")
fun Modifier.debugBorder(color: Color = Color.Red, dp: Dp = 1.dp): Modifier = this.border(dp, color)

/**
 * A notification circle attached to the top right taking up ~35% of the available size.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/Hedvig-UI-Kit?type=design&node-id=3813%3A19134&mode=design&t=V1DM52RqO3kDFMUq-1
 */
fun Modifier.notificationCircle(showNotification: Boolean = true) = this.drawWithContent {
  drawContent()
  if (showNotification) {
    // The red circle takes up ~34% of the icon's size
    val circleDiameter = size.minDimension * 0.34375f
    val circleRadius = circleDiameter / 2
    drawCircle(
      color = Color(0xFFFF513A),
      radius = circleRadius,
      center = Offset(size.width - circleRadius, circleRadius),
    )
  }
}

@Composable
fun AnimatedBorderCard(
  modifier: Modifier = Modifier,
  shape: Shape = HedvigTheme.shapes.cornerLarge,
  borderWidth: Dp = 1.dp,
  colors: List<Color> = defaultGradientColors,
  animationDuration: Int = 2000,
  content: @Composable () -> Unit,
) {
  val gradient = Brush.sweepGradient(colors)
  val infiniteTransition = rememberInfiniteTransition(label = "InfiniteColorAnimation")
  val degrees by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = animationDuration, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "InfiniteColors",
  )
  Surface(
    modifier = modifier
      .clip(shape)
      .fillMaxWidth()
      .padding(borderWidth)
      .drawWithContent {
        rotate(degrees = degrees) {
          drawCircle(
            brush = gradient,
            radius = size.width,
            blendMode = BlendMode.SrcIn,
          )
        }
        drawContent()
      },
    color = HedvigTheme.colorScheme.surfacePrimary,
    shape = shape,
  ) {
    content()
  }

}

private val defaultGradientColors = listOf(
  Color(0xFFBC82F3),
  Color(0xFFF5B9EA),
  Color(0xFF8D99FF),
  Color(0xFFAA6EEE),
  Color(0xFFFF6778),
  Color(0xFFFFBA71),
  Color(0xFFC686FF),
)

@HedvigPreview
@Composable
private fun PreviewAnimatedBorder() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier
          .width(330.dp)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        AnimatedBorderCard {
          HedvigNotificationCard(
            priority = NotificationDefaults.NotificationPriority.Campaign,
            message = "A short message about something that needs attention.",
            withIcon = true,
            style = Default,
            modifier = Modifier,
          )
        }
      }
    }
  }
}
