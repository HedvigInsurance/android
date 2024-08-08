package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Animatable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
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
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize.Medium
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Large
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Small
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Default
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Detailed
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleColorTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleIconSizeTokens
import kotlinx.coroutines.launch

@Composable
fun HedvigToggle(
  labelText: String,
  turnedOn: Boolean,
  onClick: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  toggleStyle: ToggleStyle = ToggleDefaults.toggleStyle,
) {
  val enabled by remember(turnedOn) { mutableStateOf(turnedOn) }
  val initialContainerColor = toggleColors.containerColor(false)
  val pulsatingContainerColor = toggleColors.containerColor(true)
  val containerColor = remember { Animatable(initialContainerColor) }
  val initialLabelColor = toggleColors.labelColor(false)
  val pulsatingLabelColor = toggleColors.labelColor(true)
  val labelColor = remember { Animatable(initialLabelColor) }
  val initialDescriptionColor = toggleColors.descriptionColor(false)
  val pulsatingDescriptionColor = toggleColors.descriptionColor(true)
  val descriptionColor = remember { Animatable(initialDescriptionColor) }
  LaunchedEffect(enabled) {
    if (enabled) {
      launch {
        containerColor.animateTo(pulsatingContainerColor, animationSpec = animationSpec)
        containerColor.animateTo(initialContainerColor, animationSpec = animationSpecExit)
      }
      launch {
        labelColor.animateTo(pulsatingLabelColor, animationSpec = animationSpec)
        labelColor.animateTo(initialLabelColor, animationSpec = animationSpecExit)
      }
      launch {
        descriptionColor.animateTo(pulsatingDescriptionColor, animationSpec = animationSpec)
        descriptionColor.animateTo(initialDescriptionColor, animationSpec = animationSpecExit)
      }
    } else {
      launch { containerColor.animateTo(initialContainerColor, animationSpec = animationSpec) }
      launch { labelColor.animateTo(initialLabelColor, animationSpec = animationSpec) }
      launch { descriptionColor.animateTo(initialDescriptionColor, animationSpec = animationSpec) }
    }
  }
  when (toggleStyle) {
    is Default -> {
      DefaultToggle(
        size = toggleStyle.size,
        labelText = labelText,
        turnedOn = enabled,
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor.value,
        labelColor = labelColor.value,
      )
    }

    is Detailed -> {
      DetailedToggle(
        size = toggleStyle.size,
        labelText = labelText,
        turnedOn = enabled,
        modifier = modifier,
        onClick = onClick,
        descriptionText = toggleStyle.descriptionText,
        containerColor = containerColor.value,
        descriptionColor = descriptionColor.value,
        labelColor = labelColor.value,
      )
    }
  }
}

@Composable
private fun DefaultToggle(
  size: ToggleDefaults.ToggleDefaultStyleSize,
  containerColor: Color,
  labelText: String,
  labelColor: Color,
  turnedOn: Boolean,
  onClick: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = size.size.shape,
    color = containerColor,
    modifier = modifier,
  ) {
    Row(
      Modifier.padding(size.size.contentPadding),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      HedvigText(
        text = labelText,
        style = size.size.textStyle,
        color = labelColor,
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.width(4.dp))
      Toggle(
        enabled = turnedOn,
        onClick = onClick,
        modifier = Modifier.padding(size.size.togglePadding),
      )
    }
  }
}

@Composable
private fun DetailedToggle(
  size: ToggleDefaults.ToggleDetailedStyleSize,
  labelText: String,
  descriptionText: String,
  containerColor: Color,
  descriptionColor: Color,
  labelColor: Color,
  turnedOn: Boolean,
  onClick: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = size.size.shape,
    color = containerColor,
    modifier = modifier,
  ) {
    Column(Modifier.padding(size.size.contentPadding)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = labelText,
          style = size.size.labelTextStyle,
          color = labelColor,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(4.dp))
        Toggle(
          enabled = turnedOn,
          onClick = onClick,
        )
      }
      Spacer(Modifier.height(size.size.spacerHeight))
      HedvigText(
        text = descriptionText,
        style = size.size.descriptionTextStyle,
        color = descriptionColor,
      )
    }
  }
}

@Composable
private fun Toggle(enabled: Boolean, onClick: (Boolean) -> Unit, modifier: Modifier = Modifier) {
  val backgroundColor = toggleColors.toggleBackgroundColor(enabled)
  val interactionSource = remember { MutableInteractionSource() }
  val modifierNoIndication = modifier
    .clickable(
      role = Role.Switch,
      interactionSource = interactionSource,
      indication = null,
      onClick = {
        onClick(!enabled)
      },
    )
  Crossfade(
    // todo: looks fine without animating too, bc the is this container color change anyway
    targetState = enabled,
    animationSpec = tween(AnimationTokens().pulsatingAnimationDuration),
  ) { animatedEnabled ->
    Box(modifierNoIndication) {
      ToggleBackground(backgroundColor)
      ToggleTop(
        backgroundColor = backgroundColor,
        interactionSource = interactionSource,
        onClick = {
          onClick(!animatedEnabled)
        },
        modifier = Modifier.align(if (animatedEnabled) Alignment.CenterEnd else Alignment.CenterStart),
      )
    }
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
  private val descriptionColor: Color,
  val pulsatingContainerColor: Color,
  val pulsatingLabelColor: Color,
  val pulsatingDescriptionColor: Color,
  val toggleTopColor: Color,
  private val toggleBackgroundOnColor: Color,
  private val toggleBackgroundOffColor: Color,
) {
  @Stable
  fun toggleBackgroundColor(enabled: Boolean): Color = when {
    enabled -> toggleBackgroundOnColor
    else -> toggleBackgroundOffColor
  }

  @Stable
  fun containerColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContainerColor
    else -> containerColor
  }

  @Stable
  fun labelColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingLabelColor
    else -> labelColor
  }

  @Stable
  fun descriptionColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingDescriptionColor
    else -> descriptionColor
  }
}

private sealed interface ToggleDefaultStyleSize {
  val contentPadding: PaddingValues
  val togglePadding: PaddingValues

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

    override val togglePadding: PaddingValues = PaddingValues(
      top = LargeSizeDefaultToggleTokens.ToggleTopPadding,
      bottom = LargeSizeDefaultToggleTokens.ToggleBottomPadding,
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

    override val togglePadding: PaddingValues = PaddingValues(
      top = MediumSizeDefaultToggleTokens.ToggleTopPadding,
      bottom = MediumSizeDefaultToggleTokens.ToggleBottomPadding,
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

    override val togglePadding: PaddingValues = PaddingValues(
      top = SmallSizeDefaultToggleTokens.ToggleTopPadding,
      bottom = SmallSizeDefaultToggleTokens.ToggleBottomPadding,
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

private val animationSpec = tween<Color>(AnimationTokens().pulsatingAnimationDuration)
private val animationSpecExit = tween<Color>(AnimationTokens().pulsatingAnimationDurationExit)

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
    Surface(color = Color.White) {
      var enabled by remember { mutableStateOf(false) }
      var enabled2 by remember { mutableStateOf(false) }
      var enabled3 by remember { mutableStateOf(false) }
      var enabled4 by remember { mutableStateOf(false) }
      var enabled5 by remember { mutableStateOf(false) }
      Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        HedvigToggle(
          turnedOn = enabled,
          onClick = { enabled = !enabled },
          labelText = "Large",
          toggleStyle = Default(ToggleDefaults.ToggleDefaultStyleSize.Large),
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
          HedvigToggle(
            turnedOn = enabled3,
            onClick = { enabled3 = !enabled3 },
            labelText = "Medium",
            toggleStyle = Default(Medium),
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(8.dp))
          HedvigToggle(
            turnedOn = enabled4,
            onClick = { enabled4 = !enabled4 },
            labelText = "Small",
            toggleStyle = Default(ToggleDefaults.ToggleDefaultStyleSize.Small),
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(Modifier.height(8.dp))
        Row {
          HedvigToggle(
            turnedOn = enabled2,
            onClick = { enabled2 = !enabled2 },
            labelText = "Large",
            modifier = Modifier.weight(1f),
            toggleStyle = Detailed(
              size = Large,
              descriptionText = "Long long long description Long long ",
            ),
          )
          Spacer(Modifier.width(8.dp))
          HedvigToggle(
            turnedOn = enabled5,
            onClick = { enabled5 = !enabled5 },
            labelText = "Small",
            modifier = Modifier.weight(1f),
            toggleStyle = Detailed(
              size = Small,
              descriptionText = "Long long long description Long long ",
            ),
          )
        }
        Spacer(Modifier.height(8.dp))
      }
    }
  }
}
