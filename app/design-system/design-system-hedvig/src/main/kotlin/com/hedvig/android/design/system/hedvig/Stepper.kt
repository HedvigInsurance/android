package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.launch

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
  val initialContainerColor = stepperColors.containerColor(false)
  val pulsatingContainerColor = stepperColors.containerColor(true)
  val containerColor = remember { Animatable(initialContainerColor) }
  val initialLabelColor = stepperColors.labelColor(false)
  val pulsatingLabelColor = stepperColors.labelColor(true)
  val labelColor = remember { Animatable(initialLabelColor) }
  val initialTextColor = stepperColors.textColor(false)
  val pulsatingTextColor = stepperColors.textColor(true)
  val textColor = remember { Animatable(initialTextColor) }
  val disabledSymbolColor = stepperColors.disabledSymbolColor(false)
  val enabledSymbolColor = stepperColors.enabledSymbolColor(false)
  val initialMinusColor =
    if (isMinusEnabled) enabledSymbolColor else disabledSymbolColor
  val initialPlusColor =
    if (isPlusEnabled) enabledSymbolColor else disabledSymbolColor
  val pulsatingSymbolColor = stepperColors.enabledSymbolColor(true)
  val plusColor = remember { Animatable(initialPlusColor) }
  val minusColor = remember { Animatable(initialMinusColor) }
  val enterAnimationSpec = tween<Color>(AnimationTokens().pulsatingAnimationDuration)
  val exitAnimationSpec = tween<Color>(AnimationTokens().pulsatingAnimationDurationExit)
  LaunchedEffect(isPlusEnabled, showError) {
    if (!isPlusEnabled && !showError) {
      plusColor.animateTo(disabledSymbolColor, animationSpec = exitAnimationSpec)
    } else if (!isPlusEnabled) {
      plusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
      plusColor.animateTo(disabledSymbolColor, animationSpec = exitAnimationSpec)
    } else if (showError) {
      plusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
      plusColor.animateTo(enabledSymbolColor, animationSpec = exitAnimationSpec)
    } else {
      plusColor.animateTo(enabledSymbolColor, animationSpec = exitAnimationSpec)
    }
  }
  LaunchedEffect(isMinusEnabled, showError) {
    if (!isMinusEnabled && !showError) {
      minusColor.animateTo(disabledSymbolColor, animationSpec = exitAnimationSpec)
    } else if (!isMinusEnabled) {
      minusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
      minusColor.animateTo(disabledSymbolColor, animationSpec = exitAnimationSpec)
    } else if (showError) {
      minusColor.animateTo(pulsatingSymbolColor, animationSpec = enterAnimationSpec)
      minusColor.animateTo(enabledSymbolColor, animationSpec = exitAnimationSpec)
    } else {
      minusColor.animateTo(enabledSymbolColor, animationSpec = exitAnimationSpec)
    }
  }
  LaunchedEffect(showError) {
    if (showError) {
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
    } else {
      launch { containerColor.animateTo(initialContainerColor, animationSpec = enterAnimationSpec) }
      launch { labelColor.animateTo(initialLabelColor, animationSpec = enterAnimationSpec) }
      launch { textColor.animateTo(initialTextColor, animationSpec = enterAnimationSpec) }
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
      modifier = Modifier.defaultMinSize(minHeight = stepperSize.size.minHeight(Default)),
    ) {
      Row(
        Modifier.padding(stepperSize.size.contentPadding(Default)),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = text,
          style = stepperSize.size.textStyle(Default),
          color = textColor,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(4.dp))
        StepperSymbols(
          onMinusClick,
          onPlusClick,
          plusColor,
          minusColor,
          modifier = Modifier.padding(stepperSize.size.symbolsPadding),
        )
      }
    }
    if (showError && errorText != null) {
      HedvigText(
        text = errorText,
        color = stepperColors.errorTextColor,
        style = stepperSize.size.labelTextStyle,
        modifier = Modifier.padding(stepperSize.size.errorTextPadding(Default)),
      )
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
    IconButton(onClick = onMinusClick) {
      Icon(HedvigIcons.Minus, null, tint = minusColor)
    }
    IconButton(onClick = onPlusClick) {
      Icon(HedvigIcons.Plus, null, tint = plusColor)
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
      modifier = Modifier.defaultMinSize(minHeight = stepperSize.size.minHeight(Labeled(labelText))),
    ) {
      Row(
        Modifier.padding(stepperSize.size.contentPadding(Labeled(labelText))),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
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
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(4.dp))
        StepperSymbols(onMinusClick, onPlusClick, plusColor, minusColor)
      }
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

  fun minHeight(stepperStyle: StepperStyle): Dp {
    return when (stepperStyle) {
      Default -> defaultMinHeight
      is Labeled -> labelMinHeight
    }
  }

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
