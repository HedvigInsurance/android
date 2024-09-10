package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Large
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Small
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Default
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.StepperColorTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun HedvigStepper(
  text: String,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  isPlusEnabled: Boolean,
  isMinusEnabled: Boolean,
  modifier: Modifier = Modifier,
  stepperStyle: StepperStyle = StepperDefaults.stepperStyle,
  stepperSize: StepperDefaults.StepperSize = StepperDefaults.stepperSize,
  showError: Boolean = false,
  errorText: String? = null,
) {
  val containerColor = stepperColors.containerColor(showError)
  val labelColor = stepperColors.labelColor(showError)
  val textColor = stepperColors.textColor(showError)
  val plusColor = stepperColors.symbolColor(showError, isPlusEnabled)
  val minusColor = stepperColors.symbolColor(showError, isMinusEnabled)

  when (stepperStyle) {
    Default -> DefaultStepper(
      stepperSize = stepperSize,
      text = text,
      onMinusClick = onMinusClick,
      onPlusClick = onPlusClick,
      containerColor = containerColor.value,
      textColor = textColor.value,
      plusColor = plusColor.value,
      minusColor = minusColor.value,
      errorText = errorText,
      showError = showError,
      modifier = modifier,
    )

    is Labeled -> LabeledStepper(
      stepperSize = stepperSize,
      text = text,
      labelText = stepperStyle.labelText,
      onMinusClick = onMinusClick,
      onPlusClick = onPlusClick,
      containerColor = containerColor.value,
      textColor = textColor.value,
      labelColor = labelColor.value,
      plusColor = plusColor.value,
      minusColor = minusColor.value,
      errorText = errorText,
      showError = showError,
      modifier = modifier,
    )
  }
}

@Composable
private fun DefaultStepper(
  stepperSize: StepperDefaults.StepperSize,
  text: String,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  containerColor: Color,
  textColor: Color,
  plusColor: Color,
  minusColor: Color,
  showError: Boolean,
  errorText: String?,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Surface(
      shape = stepperSize.size.shape,
      color = containerColor,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            HedvigText(
              text = text,
              style = stepperSize.size.textStyle(Default),
              color = textColor,
            )
          }
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            StepperSymbols(
              onMinusClick,
              onPlusClick,
              plusColor,
              minusColor,
              modifier = Modifier.padding(stepperSize.size.symbolsPadding),
            )
          }
        },
        spaceBetween = 4.dp,
        modifier = Modifier.padding(stepperSize.size.contentPadding(Default)),
      )
    }
    AnimatedVisibility(showError) {
      if (errorText != null) {
        HedvigText(
          text = errorText,
          color = stepperColors.errorTextColor,
          style = stepperSize.size.labelTextStyle,
          modifier = Modifier.padding(stepperSize.size.errorTextPadding(Default)),
        )
      }
    }
  }
}

@Composable
private fun StepperSymbols(
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  plusColor: Color,
  minusColor: Color,
  modifier: Modifier = Modifier,
) {
  Row(modifier) {
    IconButton(
      onClick = onMinusClick,
      modifier = Modifier.size(24.dp),
    ) {
      Icon(
        HedvigIcons.Minus,
        null,
        tint = minusColor,
      )
    }
    Spacer(Modifier.width(16.dp))
    IconButton(
      onClick = onPlusClick,
      modifier = Modifier.size(24.dp),
    ) {
      Icon(
        HedvigIcons.Plus,
        null,
        tint = plusColor,
      )
    }
  }
}

@Composable
private fun LabeledStepper(
  stepperSize: StepperDefaults.StepperSize,
  text: String,
  labelText: String,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  containerColor: Color,
  textColor: Color,
  labelColor: Color,
  plusColor: Color,
  minusColor: Color,
  errorText: String?,
  showError: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Surface(
      shape = stepperSize.size.shape,
      color = containerColor,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.padding(stepperSize.size.contentPadding(Labeled(labelText))),
        startSlot = {
          Column(
            modifier = Modifier.wrapContentSize(align = Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy((-3).dp),
          ) {
            HedvigText(
              text = labelText,
              style = stepperSize.size.labelTextStyle,
              color = labelColor,
            )
            HedvigText(
              text = text,
              style = stepperSize.size.textStyle(Labeled(labelText)),
              color = textColor,
            )
          }
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            StepperSymbols(
              onMinusClick,
              onPlusClick,
              plusColor,
              minusColor,
              modifier = Modifier.padding(stepperSize.size.symbolsPadding),
            )
          }
        },
        spaceBetween = 4.dp,
      )
    }
    if (showError && errorText != null) {
      HedvigText(
        text = errorText,
        color = stepperColors.errorTextColor,
        style = stepperSize.size.labelTextStyle,
        modifier = Modifier.padding(
          stepperSize.size.errorTextPadding(Labeled(labelText)),
        ),
      )
    }
  }
}

object StepperDefaults {
  internal val stepperStyle: StepperStyle = Default
  internal val stepperSize: StepperSize = Large

  sealed class StepperStyle {
    data object Default : StepperStyle()

    data class Labeled(val labelText: String) : StepperStyle()
  }

  enum class StepperSize {
    Large,
    Medium,
    Small,
  }
}

private val StepperDefaults.StepperSize.size: StepperSize
  get() = when (this) {
    StepperDefaults.StepperSize.Large -> StepperSize.Large
    StepperDefaults.StepperSize.Medium -> StepperSize.Medium
    StepperDefaults.StepperSize.Small -> StepperSize.Small
  }

private sealed class StepperSize {
  protected abstract val defaultContentPadding: PaddingValues
  protected abstract val labelContentPadding: PaddingValues
  protected abstract val labelMinHeight: Dp
  protected abstract val defaultMinHeight: Dp
  abstract val symbolsPadding: PaddingValues
  protected abstract val labelStyleErrorTextPadding: PaddingValues
  protected abstract val defaultStyleErrorTextPadding: PaddingValues

  @get:Composable
  protected abstract val defaultCommonTextStyle: TextStyle

  @get:Composable
  protected abstract val labeledCommonTextStyle: TextStyle

  @get:Composable
  abstract val labelTextStyle: TextStyle

  @get:Composable
  abstract val shape: Shape

  fun errorTextPadding(stepperStyle: StepperStyle): PaddingValues {
    return when (stepperStyle) {
      Default -> defaultStyleErrorTextPadding
      is Labeled -> labelStyleErrorTextPadding
    }
  }

  fun contentPadding(stepperStyle: StepperStyle): PaddingValues {
    return when (stepperStyle) {
      Default -> defaultContentPadding
      is Labeled -> labelContentPadding
    }
  }

  @Composable
  fun textStyle(stepperStyle: StepperStyle): TextStyle {
    return when (stepperStyle) {
      Default -> defaultCommonTextStyle
      is Labeled -> labeledCommonTextStyle
    }
  }

  data object Large : StepperSize() {
    override val defaultContentPadding: PaddingValues
      get() = PaddingValues(
        top = LargeSizeDefaultStepperTokens.TopPadding,
        bottom = LargeSizeDefaultStepperTokens.BottomPadding,
        start = LargeSizeDefaultStepperTokens.HorizontalPadding,
        end = LargeSizeDefaultStepperTokens.HorizontalPadding,
      )
    override val labelContentPadding: PaddingValues
      get() = PaddingValues(
        top = LargeSizeLabeledStepperTokens.TopPadding,
        bottom = LargeSizeLabeledStepperTokens.BottomPadding,
        start = LargeSizeLabeledStepperTokens.HorizontalPadding,
        end = LargeSizeLabeledStepperTokens.HorizontalPadding,
      )
    override val symbolsPadding: PaddingValues
      get() = PaddingValues(
        top = LargeSizeDefaultStepperTokens.StepperTopPadding,
        bottom = LargeSizeDefaultStepperTokens.StepperBottomPadding,
      )
    override val labelStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = LargeSizeLabeledStepperTokens.ErrorTextStartPadding,
        end = LargeSizeLabeledStepperTokens.ErrorTextEndPadding,
      )
    override val defaultStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = LargeSizeDefaultStepperTokens.ErrorTextStartPadding,
        end = LargeSizeDefaultStepperTokens.ErrorTextEndPadding,
      )
    override val defaultMinHeight: Dp
      get() = LargeSizeDefaultStepperTokens.MinHeight
    override val labelMinHeight: Dp
      get() = LargeSizeLabeledStepperTokens.MinHeight
    override val defaultCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultStepperTokens.TextFont.value
    override val labeledCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeLabeledStepperTokens.TextFont.value
    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeLabeledStepperTokens.LabelTextFont.value
    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultStepperTokens.ContainerShape.value
  }

  data object Medium : StepperSize() {
    override val defaultContentPadding: PaddingValues
      get() = PaddingValues(
        top = MediumSizeDefaultStepperTokens.TopPadding,
        bottom = MediumSizeDefaultStepperTokens.BottomPadding,
        start = MediumSizeDefaultStepperTokens.HorizontalPadding,
        end = MediumSizeDefaultStepperTokens.HorizontalPadding,
      )
    override val labelContentPadding: PaddingValues
      get() = PaddingValues(
        top = MediumSizeLabeledStepperTokens.TopPadding,
        bottom = MediumSizeLabeledStepperTokens.BottomPadding,
        start = MediumSizeLabeledStepperTokens.HorizontalPadding,
        end = MediumSizeLabeledStepperTokens.HorizontalPadding,
      )
    override val symbolsPadding: PaddingValues
      get() = PaddingValues(
        top = MediumSizeDefaultStepperTokens.StepperTopPadding,
        bottom = MediumSizeDefaultStepperTokens.StepperBottomPadding,
      )
    override val labelStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = MediumSizeLabeledStepperTokens.ErrorTextStartPadding,
        end = MediumSizeLabeledStepperTokens.ErrorTextEndPadding,
      )
    override val defaultStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = MediumSizeDefaultStepperTokens.ErrorTextStartPadding,
        end = MediumSizeDefaultStepperTokens.ErrorTextEndPadding,
      )
    override val defaultMinHeight: Dp
      get() = MediumSizeDefaultStepperTokens.MinHeight
    override val labelMinHeight: Dp
      get() = MediumSizeLabeledStepperTokens.MinHeight
    override val defaultCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultStepperTokens.TextFont.value
    override val labeledCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeLabeledStepperTokens.TextFont.value
    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeLabeledStepperTokens.LabelTextFont.value
    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultStepperTokens.ContainerShape.value
  }

  data object Small : StepperSize() {
    override val defaultContentPadding: PaddingValues
      get() = PaddingValues(
        top = SmallSizeDefaultStepperTokens.TopPadding,
        bottom = SmallSizeDefaultStepperTokens.BottomPadding,
        start = SmallSizeDefaultStepperTokens.HorizontalPadding,
        end = SmallSizeDefaultStepperTokens.HorizontalPadding,
      )
    override val labelContentPadding: PaddingValues
      get() = PaddingValues(
        top = SmallSizeLabeledStepperTokens.TopPadding,
        bottom = SmallSizeLabeledStepperTokens.BottomPadding,
        start = SmallSizeLabeledStepperTokens.HorizontalPadding,
        end = SmallSizeLabeledStepperTokens.HorizontalPadding,
      )
    override val symbolsPadding: PaddingValues
      get() = PaddingValues(
        top = SmallSizeDefaultStepperTokens.StepperTopPadding,
        bottom = SmallSizeDefaultStepperTokens.StepperBottomPadding,
      )
    override val labelStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = SmallSizeLabeledStepperTokens.ErrorTextStartPadding,
        end = SmallSizeLabeledStepperTokens.ErrorTextEndPadding,
      )
    override val defaultStyleErrorTextPadding: PaddingValues
      get() = PaddingValues(
        start = SmallSizeDefaultStepperTokens.ErrorTextStartPadding,
        end = SmallSizeDefaultStepperTokens.ErrorTextEndPadding,
      )
    override val defaultMinHeight: Dp
      get() = SmallSizeDefaultStepperTokens.MinHeight
    override val labelMinHeight: Dp
      get() = SmallSizeLabeledStepperTokens.MinHeight
    override val defaultCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultStepperTokens.TextFont.value
    override val labeledCommonTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeLabeledStepperTokens.TextFont.value
    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeLabeledStepperTokens.LabelTextFont.value
    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultStepperTokens.ContainerShape.value
  }
}

@Immutable
private data class StepperColors(
  private val containerColor: Color,
  private val labelColor: Color,
  private val textColor: Color,
  private val enabledSymbolColor: Color,
  private val disabledSymbolColor: Color,
  private val pulsatingContainerColor: Color,
  private val pulsatingContentColor: Color,
  val errorTextColor: Color,
) {
  @Composable
  fun containerColor(showError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContainerColor
      else -> containerColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun labelColor(showError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      else -> labelColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun textColor(showError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      else -> textColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun symbolColor(showError: Boolean, isEnabled: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      isEnabled -> enabledSymbolColor
      else -> disabledSymbolColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  private fun shouldPulsate(isError: Boolean): Boolean {
    var shouldPulsate by remember { mutableStateOf(false) }
    val updatedValue by rememberUpdatedState(isError)
    LaunchedEffect(Unit) {
      snapshotFlow { updatedValue }
        .drop(1)
        .collectLatest { latest ->
          if (latest) {
            shouldPulsate = true
            delay(AnimationTokens().errorPulsatingDuration.toLong())
            shouldPulsate = false
          }
        }
    }
    return shouldPulsate
  }
}

private val stepperColors: StepperColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      StepperColors(
        containerColor = fromToken(StepperColorTokens.ContainerColor),
        labelColor = fromToken(StepperColorTokens.LabelColor),
        textColor = fromToken(StepperColorTokens.TextColor),
        pulsatingContainerColor = fromToken(StepperColorTokens.PulsatingContainerColor),
        disabledSymbolColor = fromToken(StepperColorTokens.DisabledSymbolColor),
        enabledSymbolColor = fromToken(StepperColorTokens.EnabledSymbolColor),
        pulsatingContentColor = fromToken(StepperColorTokens.PulsatingContentColor),
        errorTextColor = fromToken(StepperColorTokens.LabelColor),
      )
    }
  }

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun StepperPreview() {
  HedvigTheme {
    Surface(color = Color.White) {
      Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Row {
          StepperPreviewWithParameters(
            text = "Large",
            stepperStyle = Default,
            size = Large,
            quantity = 1,
            showError = false,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = true,
            isMinusEnabled = true,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(2.dp))
          StepperPreviewWithParameters(
            text = "Medium",
            stepperStyle = Default,
            size = Medium,
            quantity = 6,
            showError = true,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = false,
            isMinusEnabled = true,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(2.dp))
          StepperPreviewWithParameters(
            text = "Small",
            stepperStyle = Default,
            size = Small,
            quantity = 0,
            showError = false,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = true,
            isMinusEnabled = false,
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(Modifier.height(8.dp))
        Row {
          StepperPreviewWithParameters(
            text = "Large",
            stepperStyle = Labeled("Label"),
            size = Large,
            quantity = 1,
            showError = false,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = true,
            isMinusEnabled = true,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(2.dp))
          StepperPreviewWithParameters(
            text = "Medium",
            stepperStyle = Labeled("Label"),
            size = Medium,
            quantity = 1,
            showError = false,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = true,
            isMinusEnabled = true,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(2.dp))
          StepperPreviewWithParameters(
            text = "Small",
            stepperStyle = Labeled("Label"),
            size = Small,
            quantity = 1,
            showError = false,
            onPlusClick = { },
            onMinusClick = { },
            isPlusEnabled = true,
            isMinusEnabled = true,
            modifier = Modifier.weight(1f),
          )
        }

        Spacer(Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun StepperPreviewWithParameters(
  stepperStyle: StepperStyle,
  size: StepperDefaults.StepperSize,
  text: String,
  quantity: Int,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  showError: Boolean,
  isPlusEnabled: Boolean,
  isMinusEnabled: Boolean,
  modifier: Modifier,
) {
  HedvigStepper(
    showError = showError,
    onPlusClick = onPlusClick,
    onMinusClick = onMinusClick,
    text = "$text: $quantity",
    stepperStyle = stepperStyle,
    stepperSize = size,
    isPlusEnabled = isPlusEnabled,
    isMinusEnabled = isMinusEnabled,
    errorText = "That would be too much or too little",
    modifier = modifier,
  )
}
