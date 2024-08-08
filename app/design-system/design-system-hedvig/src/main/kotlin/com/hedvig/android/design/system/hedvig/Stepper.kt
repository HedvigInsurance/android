package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Large
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Default
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeLabeledStepperTokens
import com.hedvig.android.design.system.hedvig.tokens.StepperColorTokens
import kotlinx.coroutines.launch

@Composable
fun HedvigStepper(
  text: String,
  onMinusClick: () -> Unit,
  onPlusClick: () -> Unit,
  isPlusEnabled: Boolean,
  isMinusEnabled: Boolean,
  modifier: Modifier = Modifier,
  stepperStyle: StepperDefaults.StepperStyle = StepperDefaults.stepperStyle,
  stepperSize: StepperDefaults.StepperSize = StepperDefaults.stepperSize,
  showError: Boolean = false,
  errorText: String? = null,
) {
  val enabled by remember(showError) { mutableStateOf(showError) }
  val initialContainerColor = stepperColors.containerColor(false)
  val pulsatingContainerColor = stepperColors.containerColor(true)
  val containerColor = remember { Animatable(initialContainerColor) }
  val initialLabelColor = stepperColors.labelColor(false)
  val pulsatingLabelColor = stepperColors.labelColor(true)
  val labelColor = remember { Animatable(initialLabelColor) }
  val initialTextColor = stepperColors.textColor(false)
  val pulsatingTextColor = stepperColors.textColor(true)
  val textColor = remember { Animatable(initialTextColor) }
  val initialMinusColor = if (isMinusEnabled) stepperColors.enabledSymbolColor(false) else stepperColors.disabledSymbolColor(false)
  val initialPlusColor = if (isPlusEnabled) stepperColors.enabledSymbolColor(false) else stepperColors.disabledSymbolColor(false)
  val pulsatingSymbolColor = stepperColors.enabledSymbolColor(true)
  val plusColor = remember { Animatable(initialPlusColor) }
  val minusColor = remember { Animatable(initialMinusColor) }
  val enterAnimationSpec = tween<Color>(AnimationTokens().pulsatingAnimationDuration)
  val exitAnimationSpec = tween<Color>(AnimationTokens().pulsatingAnimationDurationExit)
  LaunchedEffect(enabled) {
    if (enabled) {
      launch {
        containerColor.animateTo(pulsatingContainerColor, animationSpec = enterAnimationSpec)
        containerColor.animateTo(initialContainerColor, animationSpec = exitAnimationSpec)
      }
      launch {
        labelColor.animateTo(pulsatingLabelColor, animationSpec = enterAnimationSpec)
        labelColor.animateTo(initialLabelColor, animationSpec = exitAnimationSpec)
      }
      launch {
        textColor.animateTo(pulsatingTextColor, animationSpec = enterAnimationSpec)
        textColor.animateTo(initialTextColor, animationSpec = exitAnimationSpec)
      }
      launch {
        plusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
        plusColor.animateTo(initialPlusColor, animationSpec = exitAnimationSpec)
      }
      launch {
        minusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
        minusColor.animateTo(initialMinusColor, animationSpec = exitAnimationSpec)
      }
    } else {
      launch { containerColor.animateTo(initialContainerColor, animationSpec = enterAnimationSpec) }
      launch { labelColor.animateTo(initialLabelColor, animationSpec = enterAnimationSpec) }
      launch { textColor.animateTo(initialTextColor, animationSpec = enterAnimationSpec) }
      launch { plusColor.animateTo(initialPlusColor, animationSpec = enterAnimationSpec) }
      launch { minusColor.animateTo(initialMinusColor, animationSpec = enterAnimationSpec) }
    }
  }
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
      showError = showError,
      errorText = errorText,
      modifier = modifier
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
      showError = showError,
      errorText = errorText,
      modifier = modifier
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
  //todo
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
  showError: Boolean,
  errorText: String?,
  modifier: Modifier = Modifier,
) {
  //todo
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

  @get:Composable
  protected abstract val defaultCommonTextStyle: TextStyle

  @get:Composable
  protected abstract val labeledCommonTextStyle: TextStyle

  @get:Composable
  abstract val labelTextStyle: TextStyle

  @get:Composable
  abstract val shape: Shape

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
) {
  @Stable
  fun containerColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContainerColor
    else -> containerColor
  }

  @Stable
  fun labelColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContentColor
    else -> labelColor
  }

  @Stable
  fun textColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContentColor
    else -> textColor
  }

  @Stable
  fun enabledSymbolColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContentColor
    else -> enabledSymbolColor
  }

  @Stable
  fun disabledSymbolColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContentColor
    else -> disabledSymbolColor
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
      )
    }
  }
