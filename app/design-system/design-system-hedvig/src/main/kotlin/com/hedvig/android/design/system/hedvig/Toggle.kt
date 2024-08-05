package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Default
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Detailed
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleColorTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleIconSizeTokens

@Composable
fun HedvigToggle(
  labelText: String,
  turnedOn: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  toggleStyle: ToggleStyle = ToggleDefaults.toggleStyle,
) {
  when (toggleStyle) {
    is Default -> DefaultToggle(
      size = toggleStyle.size,
      labelText = labelText,
      turnedOn = turnedOn,
      onClick = onClick,
      modifier = modifier,
    )

    is Detailed -> DetailedToggle(
      size = toggleStyle.size,
      labelText = labelText,
      turnedOn = turnedOn,
      onClick = onClick,
      descriptionText = toggleStyle.descriptionText,
      modifier = modifier,
    )
  }
}

@Composable
private fun DefaultToggle(
  size: ToggleDefaults.ToggleDefaultStyleSize,
  labelText: String,
  turnedOn: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // todo!!

  AnimatedContent(
    targetState = turnedOn,
    transitionSpec = {
      fadeIn().togetherWith(fadeOut())
    },
  ) { animatedEnabled ->
    Toggle(
      enabled = animatedEnabled,
      onClick = onClick,
    )
  }
}

@Composable
private fun DetailedToggle(
  size: ToggleDefaults.ToggleDetailedStyleSize,
  labelText: String,
  descriptionText: String,
  turnedOn: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // todo!!

  AnimatedContent(
    targetState = turnedOn,
    transitionSpec = {
      fadeIn().togetherWith(fadeOut())
    },
  ) { animatedEnabled ->
    Toggle(
      enabled = animatedEnabled,
      onClick = onClick,
    )
  }
}

@Composable
private fun Toggle(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val backgroundColor = if (enabled) toggleColors.toggleBackgroundOnColor else toggleColors.toggleBackgroundOffColor
  val interactionSource = remember { MutableInteractionSource() }
  val modifierNoIndication = modifier
    .clickable(
      role = Role.Switch,
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick,
    )
  Box(modifierNoIndication) {
    ToggleBackground(backgroundColor)
    ToggleTop(
      backgroundColor = backgroundColor,
      interactionSource = interactionSource,
      onClick = onClick,
      modifier = Modifier.align(if (enabled) Alignment.CenterEnd else Alignment.CenterStart),
    )
  }
}

@Composable
private fun ToggleBackground(color: Color) {
  Surface(
    modifier = Modifier
      .height(toggleIconSize.height)
      .width(toggleIconSize.width),
    color = color,
    shape = ShapeDefaults.CornerLarge,
  ) {}
}

@Composable
private fun ToggleTop(
  backgroundColor: Color,
  interactionSource: MutableInteractionSource,
  onClick: () -> Unit,
  modifier: Modifier,
) {
  Surface(
    modifier = modifier
      .size(toggleIconSize.height)
      .clip(CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        onClick = { onClick() },
      ),
    color = toggleColors.toggleTopColor,
    shape = CircleShape,
    border = BorderStroke(1.dp, backgroundColor),
  ) {}
}

object ToggleDefaults {
  internal val toggleStyle: ToggleStyle = Default(ToggleDefaultStyleSize.Large)

  sealed class ToggleStyle {
    class Default(val size: ToggleDefaultStyleSize) : ToggleStyle()

    class Detailed(
      val size: ToggleDetailedStyleSize,
      val descriptionText: String,
    ) : ToggleStyle()
  }

  enum class ToggleDefaultStyleSize {
    Large,
    Medium,
    Small,
  }

  enum class ToggleDetailedStyleSize {
    Large,
    Small,
  }
}

private val ToggleDefaults.ToggleDefaultStyleSize.size: ToggleDefaultStyleSize
  get() = when (this) {
    ToggleDefaults.ToggleDefaultStyleSize.Large -> ToggleDefaultStyleSize.Large
    ToggleDefaults.ToggleDefaultStyleSize.Medium -> ToggleDefaultStyleSize.Medium
    ToggleDefaults.ToggleDefaultStyleSize.Small -> ToggleDefaultStyleSize.Small
  }

private val ToggleDefaults.ToggleDetailedStyleSize.size: ToggleDetailedStyleSize
  get() = when (this) {
    ToggleDefaults.ToggleDetailedStyleSize.Large -> ToggleDetailedStyleSize.Large
    ToggleDefaults.ToggleDetailedStyleSize.Small -> ToggleDetailedStyleSize.Small
  }

@Immutable
private data class ToggleColors(
  val containerColor: Color,
  val labelColor: Color,
  val descriptionColor: Color,
  val pulsatingContainerColor: Color,
  val pulsatingLabelColor: Color,
  val pulsatingDescriptionColor: Color,
  val toggleTopColor: Color,
  val toggleBackgroundOnColor: Color,
  val toggleBackgroundOffColor: Color,
) {
  @Stable
  internal fun toggleBackgroundColor(enabled: Boolean): Color = when {
    enabled -> toggleBackgroundOnColor
    else -> toggleBackgroundOffColor
  }

  @Stable
  internal fun containerColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContainerColor
    else -> containerColor
  }

  @Stable
  internal fun labelColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingLabelColor
    else -> labelColor
  }

  @Stable
  internal fun descriptionColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingDescriptionColor
    else -> descriptionColor
  }
}

private sealed interface ToggleDefaultStyleSize {
  val contentPadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

  @get:Composable
  val shape: Shape

  object Large : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeDefaultToggleTokens.TopPadding,
      bottom = LargeSizeDefaultToggleTokens.BottomPadding,
      start = LargeSizeDefaultToggleTokens.HorizontalPadding,
      end = LargeSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultToggleTokens.ContainerShape.value
  }

  object Medium : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeDefaultToggleTokens.TopPadding,
      bottom = MediumSizeDefaultToggleTokens.BottomPadding,
      start = MediumSizeDefaultToggleTokens.HorizontalPadding,
      end = MediumSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultToggleTokens.ContainerShape.value
  }

  object Small : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeDefaultToggleTokens.TopPadding,
      bottom = SmallSizeDefaultToggleTokens.BottomPadding,
      start = SmallSizeDefaultToggleTokens.HorizontalPadding,
      end = SmallSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultToggleTokens.ContainerShape.value
  }
}

private sealed interface ToggleDetailedStyleSize {
  val contentPadding: PaddingValues

  val spacerHeight: Dp

  @get:Composable
  val labelTextStyle: TextStyle

  @get:Composable
  val descriptionTextStyle: TextStyle

  @get:Composable
  val shape: Shape

  object Large : ToggleDetailedStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeDetailedToggleTokens.TopPadding,
      bottom = LargeSizeDetailedToggleTokens.BottomPadding,
      start = LargeSizeDetailedToggleTokens.HorizontalPadding,
      end = LargeSizeDetailedToggleTokens.HorizontalPadding,
    )

    override val spacerHeight: Dp = LargeSizeDetailedToggleTokens.SpacerHeight

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.LabelTextFont.value

    override val descriptionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.DescriptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.ContainerShape.value
  }

  object Small : ToggleDetailedStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeDetailedToggleTokens.TopPadding,
      bottom = SmallSizeDetailedToggleTokens.BottomPadding,
      start = SmallSizeDetailedToggleTokens.HorizontalPadding,
      end = SmallSizeDetailedToggleTokens.HorizontalPadding,
    )

    override val spacerHeight: Dp = SmallSizeDetailedToggleTokens.SpacerHeight

    override val descriptionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.DescriptionTextFont.value

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.ContainerShape.value
  }
}

private val toggleColors: ToggleColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      ToggleColors(
        containerColor = fromToken(ToggleColorTokens.ContainerColor),
        labelColor = fromToken(ToggleColorTokens.LabelColor),
        descriptionColor = fromToken(ToggleColorTokens.DescriptionColor),
        pulsatingContainerColor = fromToken(ToggleColorTokens.PulsatingContainerColor),
        pulsatingLabelColor = fromToken(ToggleColorTokens.PulsatingLabelColor),
        pulsatingDescriptionColor = fromToken(ToggleColorTokens.PulsatingDescriptionColor),
        toggleTopColor = fromToken(ToggleColorTokens.ToggleTopColor),
        toggleBackgroundOnColor = fromToken(ToggleColorTokens.ToggleBackgroundOnColor),
        toggleBackgroundOffColor = fromToken(ToggleColorTokens.ToggleBackgroundOffColor),
      )
    }
  }

private data class ToggleIconSize(
  val height: Dp,
  val width: Dp,
)

private val toggleIconSize: ToggleIconSize = ToggleIconSize(
  height = ToggleIconSizeTokens.ToggleHeight,
  width = ToggleIconSizeTokens.ToggleWidth,
)

@Preview
@Composable
private fun TogglePreview() {
  HedvigTheme {
    Surface {
      var enabled by remember { mutableStateOf(false) }
      Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        HedvigToggle(
          turnedOn = enabled,
          onClick = { enabled = !enabled },
          labelText = "Label",
        )
        Spacer(Modifier.height(8.dp))
      }
    }
  }
}
