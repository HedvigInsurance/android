package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.BottomCenter
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.BottomEnd
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.BottomStart
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.End
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.Start
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopCenter
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopStart
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Campaign
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Campaign.Brightness.BLEAK
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Campaign.Brightness.BRIGHT
import com.hedvig.android.design.system.hedvig.TooltipDefaults.arrowHeightDp
import com.hedvig.android.design.system.hedvig.TooltipDefaults.arrowWidthDp
import com.hedvig.android.design.system.hedvig.TooltipDefaults.defaultStyle
import com.hedvig.android.design.system.hedvig.TooltipDefaults.iconWidth
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.TooltipTokens
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun HedvigTooltip(
  message: String,
  showTooltip: Boolean,
  tooltipShown: () -> Unit,
  modifier: Modifier = Modifier,
  tooltipStyle: TooltipStyle = defaultStyle,
  beakDirection: BeakDirection = BottomCenter,
  maxWidth: Dp = TooltipDefaults.defaultMaxWidth,
) {
  var transientShowTooltip by remember { mutableStateOf(false) }
  LaunchedEffect(showTooltip) {
    if (!showTooltip) return@LaunchedEffect
    delay(0.5.seconds)
    transientShowTooltip = showTooltip
    tooltipShown()
    delay(5.seconds)
    transientShowTooltip = false
  }
  InnerChatTooltip(
    message = message,
    show = transientShowTooltip,
    onClick = {
      transientShowTooltip = false
      tooltipShown()
    },
    modifier = modifier,
    tooltipStyle = tooltipStyle,
    beakDirection = beakDirection,
    maxWidth = maxWidth,
  )
}

@Composable
private fun InnerChatTooltip(
  message: String,
  show: Boolean,
  onClick: () -> Unit,
  tooltipStyle: TooltipStyle,
  beakDirection: BeakDirection,
  modifier: Modifier = Modifier,
  maxWidth: Dp = TooltipDefaults.defaultMaxWidth,
) {
  val generalOffsetY = when (beakDirection) {
    BottomCenter, BottomStart, BottomEnd -> arrowHeightDp
    TopCenter, TopStart, TopEnd -> -arrowHeightDp
    Start -> 0.dp
    End -> 0.dp
  }
  val generalOffsetX = 0.dp
  Box(
    modifier
      .widthIn(max = maxWidth)
      .wrapContentHeight(Alignment.Top, true)
      .wrapContentWidth(Alignment.End, true)
      .offset(y = generalOffsetY, x = generalOffsetX),
  ) {
    Crossfade(show, label = "tooltip") { crossfadeShow ->
      if (crossfadeShow) {
        val shape = TooltipDefaults.shape
        Surface(
          onClick = onClick,
          color = tooltipStyle.containerColor,
          shape = remember(shape) { shape.withBeak(beakDirection) },
        ) {
          val padding = when (beakDirection) {
            BottomCenter, BottomEnd, BottomStart -> TooltipDefaults.paddingForBottomBeak
            TopCenter, TopStart, TopEnd -> TooltipDefaults.paddingForTopBeak
            Start -> TooltipDefaults.paddingForStartBeak
            End -> TooltipDefaults.paddingForEndBeak
          }
          Column(
            modifier = Modifier
              .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            HedvigText(
              text = message,
              style = TooltipDefaults.textStyle,
              textAlign = TextAlign.Center,
            )
            if (tooltipStyle is Campaign) {
              HedvigText(
                color = tooltipStyle.subMessageColor,
                style = TooltipDefaults.textStyle,
                text = tooltipStyle.subMessage,
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      }
    }
  }
}

private fun Shape.withBeak(beakDirection: BeakDirection): Shape {
  return object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
      val iconWidth: Float = with(density) { iconWidth.toPx() }
      val arrowWidth = with(density) { arrowWidthDp.toPx() }
      val arrowHeight = with(density) { arrowHeightDp.toPx() }
      val squircleSize: Size = when (beakDirection) {
        BottomCenter, BottomStart, BottomEnd, TopCenter, TopStart, TopEnd -> size.copy(
          height =
            size.height - arrowHeight,
        )
        Start, End -> size.copy(width = size.width - arrowHeight)
      }
      val squircleOutline = this@withBeak.createOutline(
        squircleSize,
        layoutDirection,
        density,
      )
      val squirclePath: Path = (squircleOutline as Outline.Generic).path
      val beakPath = when (beakDirection) {
        BottomCenter, BottomStart, BottomEnd -> beakToBottom(density, arrowWidth)
        TopCenter, TopStart, TopEnd -> beakToTop(density, arrowWidth)
        Start -> beakToStart(density, arrowWidth)
        End -> beakToEnd(density, arrowWidth)
      }
      val squircleOffset: Offset = when (beakDirection) {
        BottomCenter, BottomStart, BottomEnd -> Offset(x = 0f, y = -arrowHeight)
        TopCenter, TopStart, TopEnd -> Offset(x = 0f, y = arrowHeight)
        Start -> Offset(x = arrowHeight, y = 0f)
        End -> Offset(0f, y = 0f)
      }
      val beakOffset: Offset = when (beakDirection) {
        BottomCenter -> Offset(x = size.width / 2, y = size.height - 2 * arrowHeight)
        BottomStart -> Offset(x = iconWidth / 2, y = size.height - 2 * arrowHeight)
        BottomEnd -> Offset(x = size.width - (iconWidth / 2), y = size.height - 2 * arrowHeight)
        TopEnd -> Offset(x = size.width - (iconWidth / 2), y = arrowHeight)
        TopCenter -> Offset(x = size.width / 2, y = arrowHeight)
        TopStart -> Offset(x = iconWidth / 2, y = arrowHeight)
        Start -> Offset(x = arrowHeight, y = size.height / 2)
        End -> Offset(x = size.width - arrowHeight, y = size.height / 2)
      }
      return Outline.Generic(
        Path().apply {
          addPath(path = squirclePath, offset = squircleOffset)
          addPath(path = beakPath, offset = beakOffset)
        },
      )
    }
  }
}

private fun beakToTop(density: Density, arrowWidth: Float): Path {
  return Path().apply {
    // first 4 dps are straight, the tip is only curved
    val straightLineSize = with(density) { 4.dp.toPx() }
    val halfArrowWidth = arrowWidth / 2
    relativeLineTo(-halfArrowWidth, 0f)
    // How far further right the first control point and further left the second control point are in order to
    // achieve the desired curve
    val bezierOverlap = -2.5f
    // required so that the arrow height is actually as high as it must, since bezier curves need to overshoot a
    // bit on their control points to actually reach the desired height
    val bezierVerticalOvershoot = 4f
    relativeLineTo(straightLineSize, -straightLineSize)
    cubicTo(
      x1 = bezierOverlap,
      y1 = -straightLineSize - bezierVerticalOvershoot,
      x2 = -bezierOverlap,
      y2 = -straightLineSize - bezierVerticalOvershoot,
      x3 = halfArrowWidth - straightLineSize,
      y3 = -straightLineSize,
    )
    relativeLineTo(straightLineSize, straightLineSize)
    close()
  }
}

private fun beakToBottom(density: Density, arrowWidth: Float): Path {
  return Path().apply {
    val straightLineSize = with(density) { 4.dp.toPx() }
    val halfArrowWidth = arrowWidth / 2
    relativeLineTo(-halfArrowWidth, 0f)
    val bezierOverlap = -2.5f
    val bezierVerticalOvershoot = 4f
    relativeLineTo(straightLineSize, straightLineSize)
    cubicTo(
      x1 = bezierOverlap,
      y1 = straightLineSize + bezierVerticalOvershoot,
      x2 = -bezierOverlap,
      y2 = straightLineSize + bezierVerticalOvershoot,
      x3 = halfArrowWidth - straightLineSize,
      y3 = straightLineSize,
    )
    relativeLineTo(straightLineSize, -straightLineSize)
    close()
  }
}

private fun beakToStart(density: Density, arrowWidth: Float): Path {
  return Path().apply {
    val straightLineSize = with(density) { 4.dp.toPx() }
    val halfArrowWidth = arrowWidth / 2
    relativeLineTo(0f, halfArrowWidth)
    val bezierOverlap = -2.5f
    val bezierHorizontalOvershoot = 4f
    relativeLineTo(-straightLineSize, -straightLineSize)
    cubicTo(
      x1 = -straightLineSize - bezierHorizontalOvershoot,
      y1 = bezierOverlap,
      x2 = -straightLineSize - bezierHorizontalOvershoot,
      y2 = -bezierOverlap,
      x3 = -straightLineSize,
      y3 = -halfArrowWidth + straightLineSize,
    )
    relativeLineTo(straightLineSize, -straightLineSize)
    close()
  }
}

private fun beakToEnd(density: Density, arrowWidth: Float): Path {
  return Path().apply {
    val straightLineSize = with(density) { 4.dp.toPx() }
    val halfArrowWidth = arrowWidth / 2
    relativeLineTo(0f, halfArrowWidth)
    val bezierOverlap = -2.5f
    val bezierHorizontalOvershoot = 4f
    relativeLineTo(dx = straightLineSize, dy = -straightLineSize)
    cubicTo(
      x1 = straightLineSize + bezierHorizontalOvershoot,
      y1 = bezierOverlap,
      x2 = straightLineSize + bezierHorizontalOvershoot,
      y2 = -bezierOverlap,
      x3 = straightLineSize,
      y3 = -halfArrowWidth + straightLineSize,
    )
    relativeLineTo(-straightLineSize, -straightLineSize)
    close()
  }
}

object TooltipDefaults {
  val textStyle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = TooltipTokens.TextFont.value
  val defaultMaxWidth = TooltipTokens.DefaultMaxWidth
  val defaultStyle = TooltipStyle.Default
  val arrowHeightDp = TooltipTokens.ArrowHeightDp
  val arrowWidthDp = TooltipTokens.ArrowWidthDp
  val iconWidth = TooltipTokens.IconWidth
  val shape
    @Composable
    @ReadOnlyComposable
    get() = TooltipTokens.ContainerShape.value
  val paddingForTopBeak = PaddingValues(
    start = TooltipTokens.PaddingStart,
    end = TooltipTokens.PaddingEnd,
    top = TooltipTokens.PaddingTop + arrowHeightDp,
    bottom = TooltipTokens.PaddingBottom,
  )
  val paddingForBottomBeak = PaddingValues(
    start = TooltipTokens.PaddingStart,
    end = TooltipTokens.PaddingEnd,
    bottom = TooltipTokens.PaddingBottom + arrowHeightDp + TooltipTokens.PaddingTop,
  )
  val paddingForStartBeak = PaddingValues(
    start = TooltipTokens.PaddingStart + arrowHeightDp,
    end = TooltipTokens.PaddingEnd,
    top = TooltipTokens.PaddingTop,
    bottom = TooltipTokens.PaddingBottom,
  )
  val paddingForEndBeak = PaddingValues(
    start = TooltipTokens.PaddingStart,
    end = TooltipTokens.PaddingEnd + arrowHeightDp,
    top = TooltipTokens.PaddingTop,
    bottom = TooltipTokens.PaddingBottom,
  )

  enum class BeakDirection {
    BottomCenter,
    BottomStart,
    BottomEnd,
    TopCenter,
    TopStart,
    TopEnd,
    Start,
    End,
  }

  sealed class TooltipStyle {
    @get:Composable
    abstract val textColor: Color

    @get:Composable
    abstract val containerColor: Color

    data object Default : TooltipStyle() {
      override val textColor: Color
        @Composable
        get() = tooltipColors.defaultTextColor
      override val containerColor: Color
        @Composable
        get() = tooltipColors.defaultContainerColor
    }

    data class Campaign(
      val subMessage: String,
      val brightness: Brightness,
    ) : TooltipStyle() {
      enum class Brightness {
        BRIGHT,
        BLEAK,
      }

      override val textColor: Color
        @Composable
        get() = when (brightness) {
          BRIGHT -> tooltipColors.brightTextColor
          BLEAK -> tooltipColors.bleakTextColor
        }
      override val containerColor: Color
        @Composable
        get() = when (brightness) {
          BRIGHT -> tooltipColors.brightContainerColor
          BLEAK -> tooltipColors.bleakContainerColor
        }

      val subMessageColor: Color
        @Composable
        get() = when (brightness) {
          BRIGHT -> tooltipColors.brightSubMessageColor
          BLEAK -> tooltipColors.bleakSubMessageColor
        }
    }
  }
}

@Immutable
private data class TooltipColors(
  val defaultContainerColor: Color,
  val defaultTextColor: Color,
  val brightContainerColor: Color,
  val brightTextColor: Color,
  val brightSubMessageColor: Color,
  val bleakContainerColor: Color,
  val bleakTextColor: Color,
  val bleakSubMessageColor: Color,
)

private val tooltipColors: TooltipColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      TooltipColors(
        defaultContainerColor = fromToken(ColorSchemeKeyTokens.FillPrimary),
        defaultTextColor = fromToken(ColorSchemeKeyTokens.TextNegative),
        brightContainerColor = fromToken(ColorSchemeKeyTokens.SignalGreenFill),
        brightTextColor = fromToken(ColorSchemeKeyTokens.TextBlack),
        brightSubMessageColor = fromToken(ColorSchemeKeyTokens.SignalGreenText),
        bleakContainerColor = fromToken(ColorSchemeKeyTokens.SurfaceSecondary),
        bleakTextColor = fromToken(ColorSchemeKeyTokens.TextPrimary),
        bleakSubMessageColor = fromToken(ColorSchemeKeyTokens.TextSecondary),
      )
    }
  }

@Preview
@Composable
private fun PreviewRadioOptionStyles(
  @PreviewParameter(TooltipStyleProvider::class) style: TooltipStyle,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(
        Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = TopEnd, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = TopStart, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = TopCenter, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = BottomEnd, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = BottomStart, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = BottomCenter, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = Start, tooltipStyle = style)
        Spacer(Modifier.height(16.dp))
        InnerChatTooltip("50% off for 3 months", true, {}, beakDirection = End, tooltipStyle = style)
      }
    }
  }
}

private class TooltipStyleProvider :
  CollectionPreviewParameterProvider<TooltipStyle>(
    listOf(
      TooltipStyle.Default,
      Campaign("Then you pay 399 kr/mo", BRIGHT),
      Campaign("Then you pay 399 kr/mo", BLEAK),
    ),
  )
