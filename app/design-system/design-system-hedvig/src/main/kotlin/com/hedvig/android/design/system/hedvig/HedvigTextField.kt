@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.internal.HedvigDecorationBox
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeTextFieldTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeTextFieldTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeTextFieldTokens
import com.hedvig.android.design.system.hedvig.tokens.TextFieldTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun HedvigTextField(
  text: String,
  onValueChange: (String) -> Unit,
  labelText: String,
  textFieldSize: HedvigTextFieldDefaults.TextFieldSize,
  modifier: Modifier = Modifier,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  errorState: HedvigTextFieldDefaults.ErrorState = HedvigTextFieldDefaults.ErrorState.NoError,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource? = null,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val configuration = HedvigTextFieldDefaults.configuration()
  val size = textFieldSize.size
  val colors = HedvigTextFieldDefaults.colors()
  val trailingIconColor by colors.trailingContentColor(
    readOnly = readOnly,
    enabled = enabled,
    isError = errorState is HedvigTextFieldDefaults.ErrorState.Error,
  )
  val isFocused by interactionSource.collectIsFocusedAsState()
  HedvigTextField(
    value = text,
    onValueChange = onValueChange,
    colors = colors,
    configuration = configuration,
    size = size,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    label = { HedvigText(text = labelText) },
    suffix = suffix,
    leadingContent = leadingContent,
    trailingContent = TrailingContent(
      trailingContent = trailingContent,
      errorState = errorState,
      trailingIconColor = trailingIconColor,
      readOnly = readOnly,
      isFocused = isFocused,
      text = text,
      enabled = enabled,
      clearText = { onValueChange("") },
    ),
    supportingText = if (errorState is HedvigTextFieldDefaults.ErrorState.Error.WithMessage) {
      { HedvigText(text = errorState.message) }
    } else {
      null
    },
    isError = errorState.isError,
    visualTransformation = visualTransformation,
    onTextLayout = onTextLayout,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    interactionSource = interactionSource,
  )
}

@Composable
private fun TrailingContent(
  trailingContent: @Composable() (() -> Unit)?,
  errorState: ErrorState,
  trailingIconColor: Color,
  readOnly: Boolean,
  isFocused: Boolean,
  text: String,
  enabled: Boolean,
  clearText: () -> Unit,
) = when {
  trailingContent != null -> {
    trailingContent
  }

  errorState.isError -> {
    { ErrorTrailingIcon(trailingIconColor) }
  }

  readOnly -> {
    { ReadOnlyTrailingIcon(trailingIconColor) }
  }

  isFocused && text.isNotEmpty() -> {
    {
      IsNotEmptyTrailingIcon(
        trailingIconColor,
        {
          if (enabled && !readOnly) {
            clearText()
          }
        },
      )
    }
  }

  else -> {
    null
  }
}

@Composable
fun HedvigTextField(
  textValue: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  labelText: String,
  textFieldSize: HedvigTextFieldDefaults.TextFieldSize,
  modifier: Modifier = Modifier,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  errorState: HedvigTextFieldDefaults.ErrorState = HedvigTextFieldDefaults.ErrorState.NoError,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource? = null,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val configuration = HedvigTextFieldDefaults.configuration()
  val size = textFieldSize.size
  val colors = HedvigTextFieldDefaults.colors()
  val trailingIconColor by colors.trailingContentColor(
    readOnly = readOnly,
    enabled = enabled,
    isError = errorState is HedvigTextFieldDefaults.ErrorState.Error,
  )
  val isFocused by interactionSource.collectIsFocusedAsState()
  HedvigTextField(
    value = textValue,
    onValueChange = onValueChange,
    colors = colors,
    configuration = configuration,
    size = size,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    label = { HedvigText(text = labelText) },
    suffix = suffix,
    leadingContent = leadingContent,
    trailingContent = TrailingContent(
      trailingContent = trailingContent,
      errorState = errorState,
      trailingIconColor = trailingIconColor,
      readOnly = readOnly,
      isFocused = isFocused,
      text = textValue.text,
      enabled = enabled,
      clearText = { onValueChange(TextFieldValue("")) },
    ),
    supportingText = if (errorState is HedvigTextFieldDefaults.ErrorState.Error.WithMessage) {
      { HedvigText(text = errorState.message) }
    } else {
      null
    },
    isError = errorState.isError,
    visualTransformation = visualTransformation,
    onTextLayout = onTextLayout,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    interactionSource = interactionSource,
  )
}

/**
 * Similar to above, without an `ErrorState` parameter, and it allows a custom supportingText slot,
 */
@Composable
fun HedvigTextField(
  text: String,
  onValueChange: (String) -> Unit,
  labelText: String,
  textFieldSize: HedvigTextFieldDefaults.TextFieldSize,
  supportingText: @Composable (() -> Unit)?,
  modifier: Modifier = Modifier,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource? = null,
) {
  HedvigTextField(
    value = text,
    onValueChange = onValueChange,
    colors = HedvigTextFieldDefaults.colors(),
    configuration = HedvigTextFieldDefaults.configuration(),
    size = textFieldSize.size,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    label = { HedvigText(text = labelText) },
    suffix = suffix,
    leadingContent = leadingContent,
    supportingText = supportingText,
    isError = false,
    visualTransformation = visualTransformation,
    onTextLayout = onTextLayout,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    interactionSource = interactionSource ?: remember { MutableInteractionSource() },
  )
}

@Composable
fun HedvigTextField(
  state: TextFieldState,
  labelText: String,
  textFieldSize: HedvigTextFieldDefaults.TextFieldSize,
  modifier: Modifier = Modifier,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  errorState: HedvigTextFieldDefaults.ErrorState = HedvigTextFieldDefaults.ErrorState.NoError,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  inputTransformation: InputTransformation? = null,
  onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActionHandler? = null,
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val configuration = HedvigTextFieldDefaults.configuration()
  val size = textFieldSize.size
  val colors = HedvigTextFieldDefaults.colors()
  val trailingIconColor by colors.trailingContentColor(
    readOnly = readOnly,
    enabled = enabled,
    isError = errorState is HedvigTextFieldDefaults.ErrorState.Error,
  )
  val isFocused by interactionSource.collectIsFocusedAsState()
  HedvigTextField(
    state = state,
    colors = colors,
    configuration = configuration,
    size = size,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    label = { HedvigText(text = labelText) },
    suffix = suffix,
    leadingContent = leadingContent,
    trailingContent = TrailingContent(
      trailingContent = trailingContent,
      errorState = errorState,
      trailingIconColor = trailingIconColor,
      readOnly = readOnly,
      isFocused = isFocused,
      text = state.text.toString(),
      enabled = enabled,
      clearText = { state.clearText() },
    ),
    supportingText = if (errorState is HedvigTextFieldDefaults.ErrorState.Error.WithMessage) {
      { HedvigText(text = errorState.message) }
    } else {
      null
    },
    isError = errorState.isError,
    inputTransformation = inputTransformation,
    onTextLayout = onTextLayout,
    lineLimits = lineLimits,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    interactionSource = interactionSource,
  )
}

// region text field configuration

object HedvigTextFieldDefaults {
  enum class TextFieldSize {
    Large,
    Medium,
    Small,
  }

  sealed interface ErrorState {
    val isError: Boolean

    data object NoError : ErrorState {
      override val isError: Boolean = false
    }

    sealed interface Error : ErrorState {
      override val isError: Boolean
        get() = true

      data object WithoutMessage : Error

      data class WithMessage(val message: String) : Error
    }
  }
}

@Composable
internal fun HedvigTextFieldDefaults.colors(
  containerColor: Color = TextFieldTokens.ContainerColor.value,
  containerPulsatingColor: Color = TextFieldTokens.ContainerPulsatingColor.value,
  textColor: Color = TextFieldTokens.TextColor.value,
  textDisabledColor: Color = TextFieldTokens.TextDisabledColor.value,
  textLabelColor: Color = TextFieldTokens.TextLabelColor.value,
  textDisabledLabelColor: Color = TextFieldTokens.TextDisabledLabelColor.value,
  supportingTextColor: Color = TextFieldTokens.SupportingTextColor.value,
  errorPulsatingContainerColor: Color = TextFieldTokens.ErrorPulsatingContainerColor.value,
  errorPulsatingTextColor: Color = TextFieldTokens.ErrorPulsatingTextColor.value,
  errorPulsatingTextLabelColor: Color = TextFieldTokens.ErrorPulsatingTextLabelColor.value,
  xIconColor: Color = TextFieldTokens.XIconColor.value,
  disabledXIconColor: Color = TextFieldTokens.DisabledXIconColor.value,
  lockIconColor: Color = TextFieldTokens.LockIconColor.value,
  warningIconColor: Color = TextFieldTokens.WarningIconColor.value,
  borderColor: Color = TextFieldTokens.BorderColor.value,
  textSelectionColors: TextSelectionColors = LocalTextSelectionColors.current,
): HedvigTextFieldColors {
  return HedvigTextFieldColors(
    containerColor = containerColor,
    containerPulsatingColor = containerPulsatingColor,
    textColor = textColor,
    textDisabledColor = textDisabledColor,
    textLabelColor = textLabelColor,
    textDisabledLabelColor = textDisabledLabelColor,
    supportingTextColor = supportingTextColor,
    errorPulsatingContainerColor = errorPulsatingContainerColor,
    errorPulsatingTextColor = errorPulsatingTextColor,
    errorPulsatingTextLabelColor = errorPulsatingTextLabelColor,
    xIconColor = xIconColor,
    disabledXIconColor = disabledXIconColor,
    lockIconColor = lockIconColor,
    warningIconColor = warningIconColor,
    borderColor = borderColor,
    textSelectionColors = textSelectionColors,
  )
}

@Composable
internal fun HedvigTextFieldDefaults.configuration(
  shape: Shape = TextFieldTokens.Shape.value,
  borderWidth: Dp = TextFieldTokens.BorderWidth,
  focusedBorderWidth: Dp = TextFieldTokens.FocusedBorderWidth,
  errorBorderWidth: Dp = TextFieldTokens.ErrorBorderWidth,
  supportingTextStyle: TextStyle = TextFieldTokens.SupportingTextStyle.value,
  textFieldToOtherContentHorizontalPadding: Dp = TextFieldTokens.TextFieldToOtherContentHorizontalPadding,
): HedvigTextFieldConfiguration = HedvigTextFieldConfiguration(
  shape = shape,
  borderWidth = borderWidth,
  focusedBorderWidth = focusedBorderWidth,
  errorBorderWidth = errorBorderWidth,
  supportingTextStyle = supportingTextStyle,
  textFieldToOtherContentHorizontalPadding = textFieldToOtherContentHorizontalPadding,
)

@Immutable
internal data class HedvigTextFieldConfiguration(
  val shape: Shape,
  val borderWidth: Dp,
  val focusedBorderWidth: Dp,
  val errorBorderWidth: Dp,
  val supportingTextStyle: TextStyle,
  val textFieldToOtherContentHorizontalPadding: Dp,
)

@Immutable
internal data class HedvigTextFieldColors internal constructor(
  private val containerColor: Color,
  private val containerPulsatingColor: Color,
  private val textColor: Color,
  private val textDisabledColor: Color,
  private val textLabelColor: Color,
  private val textDisabledLabelColor: Color,
  private val supportingTextColor: Color,
  private val errorPulsatingContainerColor: Color,
  private val errorPulsatingTextColor: Color,
  private val errorPulsatingTextLabelColor: Color,
  private val xIconColor: Color,
  private val disabledXIconColor: Color,
  private val lockIconColor: Color,
  private val warningIconColor: Color,
  private val borderColor: Color,
  private val textSelectionColors: TextSelectionColors,
) {
  @Composable
  internal fun trailingContentColor(readOnly: Boolean, enabled: Boolean, isError: Boolean): State<Color> {
    return rememberUpdatedState(
      when {
        isError -> warningIconColor
        readOnly -> lockIconColor
        !enabled -> disabledXIconColor
        else -> xIconColor
      },
    )
  }

  @Composable
  internal fun containerColor(value: String, isError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(value)

    val targetValue = when {
      shouldPulsate && isError -> errorPulsatingContainerColor
      shouldPulsate -> containerPulsatingColor
      else -> containerColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = SignalAnimationDuration.toInt(),
      ),
    )
  }

  @Composable
  internal fun labelColor(value: String, enabled: Boolean, isError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(value)

    val targetValue = when {
      shouldPulsate && isError -> errorPulsatingTextLabelColor
      value.isEmpty() && !enabled -> textDisabledLabelColor
      else -> textLabelColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = SignalAnimationDuration.toInt(),
      ),
    )
  }

  @Composable
  internal fun textColor(value: String, enabled: Boolean, isError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(value)

    val targetValue = when {
      shouldPulsate && isError -> errorPulsatingTextColor
      !enabled -> textDisabledColor
      else -> textColor
    }
    return rememberUpdatedState(targetValue)
  }

  internal fun supportingTextColor(): Color = supportingTextColor

  internal val cursorColor: Color = textSelectionColors.handleColor

  internal val selectionColors: TextSelectionColors = textSelectionColors

  @Composable
  private fun shouldPulsate(value: String): Boolean {
    var shouldPulsate by remember { mutableStateOf(false) }
    val updatedValue by rememberUpdatedState(value)
    LaunchedEffect(Unit) {
      snapshotFlow { updatedValue }
        .drop(1)
        .collectLatest {
          shouldPulsate = true
          delay(SignalAnimationDuration)
          shouldPulsate = false
        }
    }
    return shouldPulsate
  }
}

private val HedvigTextFieldDefaults.TextFieldSize.size: HedvigTextFieldSize
  get() = when (this) {
    HedvigTextFieldDefaults.TextFieldSize.Large -> HedvigTextFieldSize.Large
    HedvigTextFieldDefaults.TextFieldSize.Medium -> HedvigTextFieldSize.Medium
    HedvigTextFieldDefaults.TextFieldSize.Small -> HedvigTextFieldSize.Small
  }

internal interface HedvigTextFieldSizeConstants {
  val topPadding: Dp
  val topPaddingWithTextAndLabel: Dp
  val bottomPadding: Dp
  val bottomPaddingWithTextAndLabel: Dp
  val horizontalPadding: Dp
  val supportingTextHorizontalPadding: Dp
  val supportingTextTopPadding: Dp
  val supportingTextBottomPadding: Dp
  val textStyle: TypographyKeyTokens
  val labelTextStyle: TypographyKeyTokens
  val labelToTextOverlap: Dp
}

internal sealed interface HedvigTextFieldSize {
  /**
   * Padding values meant to be applied specifically above and below the text and the label.
   * This is done so that when other items are taller than the text and the label, this padding does not push the
   * container to expand according to those bigger items, but instead determines the height on the text+label.
   */
  @Composable
  fun textAndLabelVerticalPadding(onlyLabelShowing: Boolean): PaddingValues {
    val topPadding = when (onlyLabelShowing) {
      true -> hedvigTextFieldSizeConstants.topPadding
      false -> hedvigTextFieldSizeConstants.topPaddingWithTextAndLabel
    }
    val bottomPadding = when (onlyLabelShowing) {
      true -> hedvigTextFieldSizeConstants.bottomPadding
      false -> hedvigTextFieldSizeConstants.bottomPaddingWithTextAndLabel
    }
    return PaddingValues(
      top = topPadding,
      bottom = bottomPadding,
    )
  }

  fun horizontalPadding(): PaddingValues {
    return PaddingValues(horizontal = hedvigTextFieldSizeConstants.horizontalPadding)
  }

  val supportingTextPadding: PaddingValues
    get() = PaddingValues(
      start = hedvigTextFieldSizeConstants.supportingTextHorizontalPadding,
      top = hedvigTextFieldSizeConstants.supportingTextTopPadding,
      end = hedvigTextFieldSizeConstants.supportingTextHorizontalPadding,
      bottom = hedvigTextFieldSizeConstants.supportingTextBottomPadding,
    )

  val textStyle: TextStyle
    @Composable
    get() = hedvigTextFieldSizeConstants.textStyle.value

  val labelTextStyle: TextStyle
    @Composable
    get() = hedvigTextFieldSizeConstants.labelTextStyle.value

  val labelToTextOverlap: Dp
    get() = hedvigTextFieldSizeConstants.labelToTextOverlap

  val hedvigTextFieldSizeConstants: HedvigTextFieldSizeConstants

  object Large : HedvigTextFieldSize {
    override val hedvigTextFieldSizeConstants: HedvigTextFieldSizeConstants = object : HedvigTextFieldSizeConstants {
      override val topPadding: Dp = LargeSizeTextFieldTokens.TopPadding
      override val topPaddingWithTextAndLabel: Dp = LargeSizeTextFieldTokens.TopPaddingWithTextAndLabel
      override val bottomPadding: Dp = LargeSizeTextFieldTokens.BottomPadding
      override val bottomPaddingWithTextAndLabel: Dp = LargeSizeTextFieldTokens.BottomPaddingWithTextAndLabel
      override val horizontalPadding: Dp = LargeSizeTextFieldTokens.HorizontalPadding
      override val supportingTextHorizontalPadding = LargeSizeTextFieldTokens.SupportingTextHorizontalPadding
      override val supportingTextTopPadding = LargeSizeTextFieldTokens.SupportingTextTopPadding
      override val supportingTextBottomPadding = LargeSizeTextFieldTokens.SupportingTextBottomPadding
      override val textStyle: TypographyKeyTokens = LargeSizeTextFieldTokens.TextStyle
      override val labelTextStyle: TypographyKeyTokens = LargeSizeTextFieldTokens.LabelTextStyle
      override val labelToTextOverlap = LargeSizeTextFieldTokens.LabelToTextOverlap
    }
  }

  object Medium : HedvigTextFieldSize {
    override val hedvigTextFieldSizeConstants: HedvigTextFieldSizeConstants = object : HedvigTextFieldSizeConstants {
      override val topPadding: Dp = MediumSizeTextFieldTokens.TopPadding
      override val topPaddingWithTextAndLabel: Dp = MediumSizeTextFieldTokens.TopPaddingWithTextAndLabel
      override val bottomPadding: Dp = MediumSizeTextFieldTokens.BottomPadding
      override val bottomPaddingWithTextAndLabel: Dp = MediumSizeTextFieldTokens.BottomPaddingWithTextAndLabel
      override val horizontalPadding: Dp = MediumSizeTextFieldTokens.HorizontalPadding
      override val supportingTextHorizontalPadding = MediumSizeTextFieldTokens.SupportingTextHorizontalPadding
      override val supportingTextTopPadding = MediumSizeTextFieldTokens.SupportingTextTopPadding
      override val supportingTextBottomPadding = MediumSizeTextFieldTokens.SupportingTextBottomPadding
      override val textStyle: TypographyKeyTokens = MediumSizeTextFieldTokens.TextStyle
      override val labelTextStyle: TypographyKeyTokens = MediumSizeTextFieldTokens.LabelTextStyle
      override val labelToTextOverlap = MediumSizeTextFieldTokens.LabelToTextOverlap
    }
  }

  object Small : HedvigTextFieldSize {
    override val hedvigTextFieldSizeConstants: HedvigTextFieldSizeConstants = object : HedvigTextFieldSizeConstants {
      override val topPadding: Dp = SmallSizeTextFieldTokens.TopPadding
      override val topPaddingWithTextAndLabel: Dp = SmallSizeTextFieldTokens.TopPaddingWithTextAndLabel
      override val bottomPadding: Dp = SmallSizeTextFieldTokens.BottomPadding
      override val bottomPaddingWithTextAndLabel: Dp = SmallSizeTextFieldTokens.BottomPaddingWithTextAndLabel
      override val horizontalPadding: Dp = SmallSizeTextFieldTokens.HorizontalPadding
      override val supportingTextHorizontalPadding = SmallSizeTextFieldTokens.SupportingTextHorizontalPadding
      override val supportingTextTopPadding = SmallSizeTextFieldTokens.SupportingTextTopPadding
      override val supportingTextBottomPadding = SmallSizeTextFieldTokens.SupportingTextBottomPadding
      override val textStyle: TypographyKeyTokens = SmallSizeTextFieldTokens.TextStyle
      override val labelTextStyle: TypographyKeyTokens = SmallSizeTextFieldTokens.LabelTextStyle
      override val labelToTextOverlap = SmallSizeTextFieldTokens.LabelToTextOverlap
    }
  }
}

// endregion

@Composable
private fun ErrorTrailingIcon(tint: Color) {
  Icon(HedvigIcons.WarningFilled, null, Modifier.size(24.dp), tint)
}

@Composable
private fun ReadOnlyTrailingIcon(tint: Color) {
  Icon(HedvigIcons.Lock, null, Modifier.size(24.dp), tint)
}

@Composable
private fun IsNotEmptyTrailingIcon(tint: Color, onClick: () -> Unit) {
  IconButton(onClick, Modifier.size(24.dp)) {
    Icon(HedvigIcons.Close, null, tint = tint)
  }
}

/**
 * The raw HedvigTextField, with the same API as the [androidx.compose.material3.TextField], with our Hedvig specific
 * HedvigTextFieldDefaults.
 */
@Composable
private fun HedvigTextField(
  value: String,
  onValueChange: (String) -> Unit,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = modifier,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = size.textStyle.merge(color = colors.textColor(value, enabled, isError).value),
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      visualTransformation = visualTransformation,
      onTextLayout = onTextLayout,
      interactionSource = interactionSource,
      cursorBrush = SolidColor(colors.cursorColor),
      decorationBox = @Composable { innerTextField ->
        HedvigTextFieldDecorationBox(
          value = value,
          colors = colors,
          configuration = configuration,
          size = size,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          label = label,
          suffix = suffix,
          leadingContent = leadingContent,
          trailingContent = trailingContent,
          supportingText = supportingText,
          enabled = enabled,
          isError = isError,
          readOnly = readOnly,
          interactionSource = interactionSource,
        )
      },
    )
  }
}

@Composable
private fun HedvigTextField(
  state: TextFieldState,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  inputTransformation: InputTransformation? = null,
  label: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActionHandler? = null,
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
    BasicTextField(
      state = state,
      modifier = modifier,
      enabled = enabled,
      readOnly = readOnly,
      inputTransformation = inputTransformation,
      textStyle = size.textStyle.merge(color = colors.textColor(state.text.toString(), enabled, isError).value),
      keyboardOptions = keyboardOptions,
      onKeyboardAction = keyboardActions,
      lineLimits = lineLimits,
      onTextLayout = onTextLayout,
      interactionSource = interactionSource,
      cursorBrush = SolidColor(colors.cursorColor),
      decorator = @Composable { innerTextField ->
        HedvigTextFieldDecorationBox(
          value = state.text.toString(),
          colors = colors,
          configuration = configuration,
          size = size,
          // Consider adapting this to the new TextFieldState API if it's required for some reason
          visualTransformation = VisualTransformation.None,
          innerTextField = innerTextField,
          label = label,
          suffix = suffix,
          leadingContent = leadingContent,
          trailingContent = trailingContent,
          supportingText = supportingText,
          enabled = enabled,
          isError = isError,
          readOnly = readOnly,
          interactionSource = interactionSource,
        )
      },
    )
  }
}

@Composable
private fun HedvigTextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = modifier,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = size.textStyle.merge(color = colors.textColor(value.text, enabled, isError).value),
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      visualTransformation = visualTransformation,
      onTextLayout = onTextLayout,
      interactionSource = interactionSource,
      cursorBrush = SolidColor(colors.cursorColor),
      decorationBox = @Composable { innerTextField ->
        HedvigTextFieldDecorationBox(
          value = value.text,
          colors = colors,
          configuration = configuration,
          size = size,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          label = label,
          suffix = suffix,
          leadingContent = leadingContent,
          trailingContent = trailingContent,
          supportingText = supportingText,
          enabled = enabled,
          isError = isError,
          readOnly = readOnly,
          interactionSource = interactionSource,
        )
      },
    )
  }
}

@Composable
private fun HedvigTextFieldDecorationBox(
  value: String,
  innerTextField: @Composable () -> Unit,
  enabled: Boolean,
  visualTransformation: VisualTransformation,
  interactionSource: InteractionSource,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  isError: Boolean = false,
  readOnly: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
) {
  HedvigDecorationBox(
    value = value,
    configuration = configuration,
    colors = colors,
    size = size,
    innerTextField = innerTextField,
    visualTransformation = visualTransformation,
    label = label,
    suffix = suffix,
    leadingContent = leadingContent,
    trailingContent = trailingContent,
    supportingText = supportingText,
    enabled = enabled,
    isError = isError,
    readOnly = readOnly,
    interactionSource = interactionSource,
  )
}

private const val SignalAnimationDuration = 400L
