package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize.Medium
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Large
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Small
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Default
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Detailed
import com.hedvig.android.design.system.hedvig.ToggleDragAnchors.End
import com.hedvig.android.design.system.hedvig.ToggleDragAnchors.Start
import com.hedvig.android.design.system.hedvig.internal.rememberAnchorDraggableState
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleColorTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleIconSizeTokens
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun HedvigToggle(
  labelText: String,
  turnedOn: Boolean,
  onClick: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  toggleStyle: ToggleStyle = ToggleDefaults.toggleStyle,
) {
  val containerColor by toggleColors.containerColor(turnedOn)
  val labelColor by toggleColors.labelColor(turnedOn)
  val descriptionColor by toggleColors.descriptionColor(turnedOn)
  when (toggleStyle) {
    is Default -> {
      DefaultToggle(
        size = toggleStyle.size,
        labelText = labelText,
        turnedOn = turnedOn,
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        labelColor = labelColor,
      )
    }

    is Detailed -> {
      DetailedToggle(
        size = toggleStyle.size,
        labelText = labelText,
        turnedOn = turnedOn,
        modifier = modifier,
        onClick = onClick,
        descriptionText = toggleStyle.descriptionText,
        containerColor = containerColor,
        descriptionColor = descriptionColor,
        labelColor = labelColor,
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
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HedvigText(
            text = labelText,
            style = size.size.textStyle,
            color = labelColor,
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Toggle(
            enabled = turnedOn,
            onClick = onClick,
            modifier = Modifier
              .padding(
                size.size.togglePadding,
              ),
          )
        }
      },
      spaceBetween = 4.dp,
      modifier = Modifier.padding(size.size.contentPadding),
    )
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
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText(
              text = labelText,
              style = size.size.labelTextStyle,
              color = labelColor,
            )
          }
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Toggle(
              enabled = turnedOn,
              onClick = onClick,
              modifier = Modifier.size(width = toggleIconSize.width, height = toggleIconSize.height),
            )
          }
        },
        spaceBetween = 4.dp,
      )
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
  val density = LocalDensity.current
  val positionalThreshold = { distance: Float -> distance * 0.3f }
  val velocityThreshold = { with(density) { 100.dp.toPx() } }
  val animationSpec = tween<Float>()
  val decayAnimationSpec = rememberSplineBasedDecay<Float>()
  val state = rememberAnchorDraggableState(
    density = density,
    initialValue = if (enabled) End else Start,
    positionalThreshold = positionalThreshold,
    velocityThreshold = velocityThreshold,
    snapAnimationSpec = animationSpec,
    decayAnimationSpec = decayAnimationSpec,
  )
  LaunchedEffect(state.settledValue) {
    when (state.settledValue) {
      Start -> if (enabled) onClick(false)
      End -> if (!enabled) onClick(true)
    }
  }
  val contentSize = toggleIconSize.height
  val contentSizePx = with(density) { contentSize.toPx() }
  val backgroundColor = toggleColors.toggleBackgroundColor(enabled)
  val interactionSource = remember { MutableInteractionSource() }
  Box {
    ToggleBackground(
      modifier =
        modifier
          .height(toggleIconSize.height)
          .width(toggleIconSize.width)
          .fillMaxSize()
          .onSizeChanged { layoutSize ->
            val dragEndPoint = layoutSize.width - contentSizePx
            state.updateAnchors(
              DraggableAnchors {
                ToggleDragAnchors.entries
                  .forEach { anchor ->
                    anchor at dragEndPoint * anchor.fraction
                  }
              },
            )
          },
      color = backgroundColor.value,
      interactionSource = interactionSource,
      contentSize = contentSize,
      draggableState = state,
      content = {
        ToggleTop(
          backgroundColor = backgroundColor.value,
        )
      },
    )
  }
}

private enum class ToggleDragAnchors(val fraction: Float) {
  Start(0f),
  End(1f),
}

@Composable
private fun ToggleBackground(
  color: Color,
  interactionSource: MutableInteractionSource,
  contentSize: Dp,
  draggableState: AnchoredDraggableState<ToggleDragAnchors>,
  content: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = color,
    shape = ShapeDefaults.CornerLarge,
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .wrapContentSize(align = Alignment.TopStart)
        .size(width = contentSize, height = contentSize)
        .offset {
          IntOffset(
            x = draggableState
              .requireOffset()
              .roundToInt(),
            y = 0,
          )
        }
        .anchoredDraggable(
          draggableState,
          Orientation.Horizontal,
          interactionSource = interactionSource,
        ),
    ) {
      content()
    }
  }
}

@Composable
private fun ToggleTop(backgroundColor: Color, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .fillMaxSize()
      .clip(CircleShape),
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
  @Composable
  fun labelColor(isTurnedOn: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(isTurnedOn)
    val targetValue = when {
      shouldPulsate -> pulsatingLabelColor
      else -> labelColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().pulsatingAnimationDuration,
      ),
    )
  }

  @Composable
  fun containerColor(isTurnedOn: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(isTurnedOn)
    val targetValue = when {
      shouldPulsate -> pulsatingContainerColor
      else -> containerColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().pulsatingAnimationDuration,
      ),
    )
  }

  @Composable
  fun descriptionColor(isTurnedOn: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(isTurnedOn)
    val targetValue = when {
      shouldPulsate -> pulsatingDescriptionColor
      else -> descriptionColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().pulsatingAnimationDuration,
      ),
    )
  }

  @Composable
  private fun shouldPulsate(isTurnedOn: Boolean): Boolean {
    var shouldPulsate by remember { mutableStateOf(false) }
    val updatedValue by rememberUpdatedState(isTurnedOn)
    LaunchedEffect(Unit) {
      snapshotFlow { updatedValue }
        .drop(1)
        .collectLatest { latest ->
          if (latest) {
            shouldPulsate = true
            delay(AnimationTokens().pulsatingAnimationDuration.toLong())
            shouldPulsate = false
          }
        }
    }
    return shouldPulsate
  }

  @Composable
  fun toggleBackgroundColor(enabled: Boolean): State<Color> {
    val targetValue = when {
      enabled -> toggleBackgroundOnColor
      else -> toggleBackgroundOffColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().pulsatingAnimationDuration,
      ),
    )
  }
}

private sealed interface ToggleDefaultStyleSize {
  val contentPadding: PaddingValues
  val togglePadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

  @get:Composable
  val shape: Shape

  data object Large : ToggleDefaultStyleSize {
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

  data object Medium : ToggleDefaultStyleSize {
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

  data object Small : ToggleDefaultStyleSize {
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

  data object Large : ToggleDetailedStyleSize {
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

  data object Small : ToggleDetailedStyleSize {
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
          labelText = "LargeLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge optionLarge option",
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
