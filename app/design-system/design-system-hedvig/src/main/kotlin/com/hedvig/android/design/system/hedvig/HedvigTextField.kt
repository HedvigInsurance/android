@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeTextFieldTokens
import com.hedvig.android.design.system.hedvig.tokens.TextFieldTokens
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
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
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
  HedvigTextField(
    value = text,
    onValueChange = onValueChange,
    colors = HedvigTextFieldDefaults.colors(),
    configuration = configuration,
    size = size,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    label = { HedvigText(text = labelText) },
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    supportingText = if (errorState is HedvigTextFieldDefaults.ErrorState.ErrorWithMessage) {
      { HedvigText(text = errorState.message) }
    } else {
      null
    },
    isError = errorState !is HedvigTextFieldDefaults.ErrorState.NoError,
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

// region text field configuration

object HedvigTextFieldDefaults {
  enum class TextFieldSize {
    Large,
    Medium,
    Small,
  }

  sealed interface ErrorState {
    data object NoError : ErrorState

    data object Error : ErrorState

    data class ErrorWithMessage(val message: String) : ErrorState
  }
}

@Composable
private fun HedvigTextFieldDefaults.colors(
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
    lockIconColor = lockIconColor,
    warningIconColor = warningIconColor,
    borderColor = borderColor,
    textSelectionColors = textSelectionColors,
  )
}

@Composable
private fun HedvigTextFieldDefaults.configuration(
  shape: Shape = TextFieldTokens.Shape.value,
  borderWidth: Dp = TextFieldTokens.BorderWidth,
  focusedBorderWidth: Dp = TextFieldTokens.FocusedBorderWidth,
  errorBorderWidth: Dp = TextFieldTokens.ErrorBorderWidth,
  supportingTextStyle: TextStyle = TextFieldTokens.SupportingTextStyle.value,
): HedvigTextFieldConfiguration = HedvigTextFieldConfiguration(
  shape = shape,
  borderWidth = borderWidth,
  focusedBorderWidth = focusedBorderWidth,
  errorBorderWidth = errorBorderWidth,
  supportingTextStyle = supportingTextStyle,
)

@Immutable
internal data class HedvigTextFieldConfiguration(
  val shape: Shape,
  val borderWidth: Dp,
  val focusedBorderWidth: Dp,
  val errorBorderWidth: Dp,
  val supportingTextStyle: TextStyle,
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
  private val lockIconColor: Color,
  private val warningIconColor: Color,
  private val borderColor: Color,
  private val textSelectionColors: TextSelectionColors,
) {
  @Composable
  internal fun trailingIconColor(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    return rememberUpdatedState(
      when {
        isError -> warningIconColor
        !enabled -> lockIconColor
        focused -> xIconColor
        else -> Color.Unspecified
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
    HedvigTextFieldDefaults.TextFieldSize.Medium -> HedvigTextFieldSize.Large // TODO()
    HedvigTextFieldDefaults.TextFieldSize.Small -> HedvigTextFieldSize.Large // TODO()
  }

internal sealed interface HedvigTextFieldSize {
  @Composable
  fun contentPadding(value: String, isFocused: Boolean): PaddingValues

  val supportingTextPadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

  @get:Composable
  val labelTextStyle: TextStyle

  val labelToTextOverlap: Dp

  object Large : HedvigTextFieldSize {
    @Composable
    override fun contentPadding(value: String, isFocused: Boolean): PaddingValues {
      val hasInput = value.isNotEmpty()
      val topPadding by animateDpAsState(
        when {
          hasInput || isFocused -> LargeSizeTextFieldTokens.TopPaddingWithLabel
          else -> LargeSizeTextFieldTokens.TopPadding
        },
      )
      val bottomPadding by animateDpAsState(
        when {
          hasInput || isFocused -> LargeSizeTextFieldTokens.BottomPaddingWithLabel
          else -> LargeSizeTextFieldTokens.BottomPadding
        },
      )
      return PaddingValues(
        start = LargeSizeTextFieldTokens.HorizontalPadding,
        top = topPadding,
        end = LargeSizeTextFieldTokens.HorizontalPadding,
        bottom = bottomPadding,
      )
    }

    override val supportingTextPadding: PaddingValues = PaddingValues(
      start = LargeSizeTextFieldTokens.SupportingTextHorizontalPadding,
      top = LargeSizeTextFieldTokens.SupportingTextTopPadding,
      end = LargeSizeTextFieldTokens.SupportingTextHorizontalPadding,
      bottom = LargeSizeTextFieldTokens.SupportingTextBottomPadding,
    )

    override val textStyle: TextStyle
      @Composable
      get() = LargeSizeTextFieldTokens.TextStyle.value

    override val labelTextStyle: TextStyle
      @Composable
      get() = LargeSizeTextFieldTokens.LabelTextStyle.value

    override val labelToTextOverlap: Dp = LargeSizeTextFieldTokens.LabelToTextOverlap
  }
}

// endregion

/**
 * The raw HedvigTextField, with the same API as the [androidx.compose.material3.TextField], with our HedvigSpecific
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
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
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
      textStyle = size.textStyle.copy(color = colors.textColor(value, enabled, isError).value),
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
        // places leading icon, text field with label, trailing icon
        HedvigTextFieldDecorationBox(
          value = value,
          colors = colors,
          configuration = configuration,
          size = size,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          label = label,
          leadingIcon = leadingIcon,
          trailingIcon = trailingIcon,
          supportingText = supportingText,
          singleLine = true,
          enabled = enabled,
          isError = isError,
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
  singleLine: Boolean,
  visualTransformation: VisualTransformation,
  interactionSource: InteractionSource,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  isError: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  container: @Composable () -> Unit = {
    ContainerBox(value, isError, colors, configuration)
  },
) {
  HedvigDecorationBox(
    value = value,
    configuration = configuration,
    colors = colors,
    size = size,
    innerTextField = innerTextField,
    visualTransformation = visualTransformation,
    label = label,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    supportingText = supportingText,
    singleLine = singleLine,
    enabled = enabled,
    isError = isError,
    interactionSource = interactionSource,
    contentPadding = size.contentPadding(value, interactionSource.collectIsFocusedAsState().value),
    container = container,
  )
}

@Composable
private fun ContainerBox(
  value: String,
  isError: Boolean,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier.background(colors.containerColor(value, isError).value, configuration.shape),
  )
}
