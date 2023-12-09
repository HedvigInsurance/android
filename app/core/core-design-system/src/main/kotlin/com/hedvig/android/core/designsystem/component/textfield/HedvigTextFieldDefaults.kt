package com.hedvig.android.core.designsystem.component.textfield

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.toColor
import com.hedvig.android.core.designsystem.material3.toShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

/**
 * Contains the default values used by [TextField]. For defaults used in [OutlinedTextField], see
 * [OutlinedTextFieldDefaults].
 */
@Immutable
object HedvigTextFieldDefaults {
  /** Default shape for a [TextField]. */
  val shape: Shape @Composable get() = HedvigTextFieldTokens.ContainerShape.toShape()

  /**
   * The default min width applied to a [TextField].
   * Note that you can override it by applying Modifier.heightIn directly on a text field.
   */
  val MinHeight = 56.dp

  /**
   * The default min width applied to a [TextField].
   * Note that you can override it by applying Modifier.widthIn directly on a text field.
   */
  val MinWidth = 280.dp

  /**
   * The default thickness of the border line in [TextField] in unfocused state.
   */
  val UnfocusedBorderThickness = 0.dp

  /**
   * The default thickness of the border line in [TextField] in focused state.
   */
  val FocusedBorderThickness = 1.dp

  /**
   * Composable that draws a default container for the content of [TextField], with a border around
   * it when focused. You can use it to draw a container for your custom text field based on
   * [HedvigTextFieldDefaults.DecorationBox]. [TextField] applies it automatically.
   *
   * @param value the value of the textfield. Used to know when it changes, to animate the color changes.
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   * @param colors [TextFieldColors] used to resolve colors of the text field
   * @param shape shape of the container
   */
  @Composable
  fun ContainerBox(
    value: String,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    colors: HedvigTextFieldColors,
    modifier: Modifier = Modifier,
    shape: Shape = HedvigTextFieldDefaults.shape,
  ) {
    Box(
      modifier.background(colors.containerColor(value, enabled, isError, interactionSource).value, shape),
    )
  }

  /**
   * A modifier to draw a default bottom indicator line in [TextField]. You can use this modifier
   * if you build your custom text field using [TextFieldDefaults.DecorationBox] whilst the
   * [TextField] applies it automatically.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   * @param colors [TextFieldColors] used to resolve colors of the text field
   * @param focusedIndicatorLineThickness thickness of the indicator line when text field is
   * focused
   * @param unfocusedIndicatorLineThickness thickness of the indicator line when text field is
   * not focused
   */
  fun Modifier.borderLine(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    colors: HedvigTextFieldColors,
    shape: Shape,
    focusedIndicatorLineThickness: Dp = FocusedBorderThickness,
    unfocusedIndicatorLineThickness: Dp = UnfocusedBorderThickness,
  ) = this.composed(
    inspectorInfo = debugInspectorInfo {
      name = "indicatorLine"
      properties["enabled"] = enabled
      properties["isError"] = isError
      properties["interactionSource"] = interactionSource
      properties["colors"] = colors
      properties["focusedIndicatorLineThickness"] = focusedIndicatorLineThickness
      properties["unfocusedIndicatorLineThickness"] = unfocusedIndicatorLineThickness
    },
  ) {
    val stroke = animateBorderStrokeAsState(
      enabled,
      isError,
      interactionSource,
      colors,
      focusedIndicatorLineThickness,
      unfocusedIndicatorLineThickness,
    )
    Modifier.drawBorderLine(stroke.value, shape)
  }

  /**
   * Default content padding applied to [TextField] when there is a label.
   *
   * Note that when the label is present, the "top" padding is a distance between the top edge of
   * the [TextField] and the top of the label, not to the top of the input field. The input field
   * is placed directly beneath the label.
   *
   * See [PaddingValues] for more details.
   */
  fun contentPaddingWithLabel(
    start: Dp = TextFieldPadding,
    end: Dp = TextFieldPadding,
    top: Dp = TextFieldWithLabelVerticalPadding,
    bottom: Dp = TextFieldWithLabelVerticalPadding,
  ): PaddingValues = PaddingValues(start, top, end, bottom)

  /**
   * Default content padding applied to [TextField] when the label is null.
   * See [PaddingValues] for more details.
   */
  fun contentPaddingWithoutLabel(
    start: Dp = TextFieldPadding,
    top: Dp = TextFieldPadding,
    end: Dp = TextFieldPadding,
    bottom: Dp = TextFieldPadding,
  ): PaddingValues = PaddingValues(start, top, end, bottom)

  /**
   * Default padding applied to supporting text for both [TextField] and [OutlinedTextField].
   * See [PaddingValues] for more details.
   */
  internal fun supportingTextPadding(
    start: Dp = SupportingHorizontalPadding,
    top: Dp = SupportingTopPadding,
    end: Dp = SupportingHorizontalPadding,
    bottom: Dp = 0.dp,
  ): PaddingValues = PaddingValues(start, top, end, bottom)

  /**
   * Creates a [TextFieldColors] that represents the default input text, container, and content
   * colors (including label, placeholder, icons, etc.) used in a [TextField].
   *
   * @param focusedTextColor the color used for the input text of this text field when focused
   * @param unfocusedTextColor the color used for the input text of this text field when not
   * focused
   * @param disabledTextColor the color used for the input text of this text field when disabled
   * @param errorTextColor the color used for the input text of this text field when in error
   * state
   * @param focusedContainerColor the container color for this text field when focused
   * @param unfocusedContainerColor the container color for this text field when not focused
   * @param disabledContainerColor the container color for this text field when disabled
   * @param errorContainerColor the container color for this text field when in error state
   * @param cursorColor the cursor color for this text field
   * @param errorCursorColor the cursor color for this text field when in error state
   * @param selectionColors the colors used when the input text of this text field is selected
   * @param focusedIndicatorColor the indicator color for this text field when focused
   * @param unfocusedIndicatorColor the indicator color for this text field when not focused
   * @param disabledIndicatorColor the indicator color for this text field when disabled
   * @param errorIndicatorColor the indicator color for this text field when in error state
   * @param focusedLeadingIconColor the leading icon color for this text field when focused
   * @param unfocusedLeadingIconColor the leading icon color for this text field when not focused
   * @param disabledLeadingIconColor the leading icon color for this text field when disabled
   * @param errorLeadingIconColor the leading icon color for this text field when in error state
   * @param focusedTrailingIconColor the trailing icon color for this text field when focused
   * @param unfocusedTrailingIconColor the trailing icon color for this text field when not
   * focused
   * @param disabledTrailingIconColor the trailing icon color for this text field when disabled
   * @param errorTrailingIconColor the trailing icon color for this text field when in error state
   * @param focusedLabelColor the label color for this text field when focused
   * @param unfocusedLabelColor the label color for this text field when not focused
   * @param disabledLabelColor the label color for this text field when disabled
   * @param errorLabelColor the label color for this text field when in error state
   * @param focusedPlaceholderColor the placeholder color for this text field when focused
   * @param unfocusedPlaceholderColor the placeholder color for this text field when not focused
   * @param disabledPlaceholderColor the placeholder color for this text field when disabled
   * @param errorPlaceholderColor the placeholder color for this text field when in error state
   * @param focusedSupportingTextColor the supporting text color for this text field when focused
   * @param unfocusedSupportingTextColor the supporting text color for this text field when not
   * focused
   * @param disabledSupportingTextColor the supporting text color for this text field when
   * disabled
   * @param errorSupportingTextColor the supporting text color for this text field when in error
   * state
   * @param focusedPrefixColor the prefix color for this text field when focused
   * @param unfocusedPrefixColor the prefix color for this text field when not focused
   * @param disabledPrefixColor the prefix color for this text field when disabled
   * @param errorPrefixColor the prefix color for this text field when in error state
   * @param focusedSuffixColor the suffix color for this text field when focused
   * @param unfocusedSuffixColor the suffix color for this text field when not focused
   * @param disabledSuffixColor the suffix color for this text field when disabled
   * @param errorSuffixColor the suffix color for this text field when in error state
   */
  @Composable
  fun colors(
    focusedTextColor: Color = HedvigTextFieldTokens.FocusInputColor.toColor(),
    unfocusedTextColor: Color = HedvigTextFieldTokens.InputColor.toColor(),
    disabledTextColor: Color = HedvigTextFieldTokens.DisabledInputColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledInputOpacity),
    errorTextColor: Color = HedvigTextFieldTokens.ErrorInputColor.toColor(),
    focusedContainerColor: Color = HedvigTextFieldTokens.ContainerColor.toColor(),
    unfocusedContainerColor: Color = HedvigTextFieldTokens.ContainerColor.toColor(),
    disabledContainerColor: Color = HedvigTextFieldTokens.ContainerColor.toColor(),
    errorContainerColor: Color = HedvigTextFieldTokens.ErrorTypeHighlightColor.toColor(),
    typingHighlightColor: Color = HedvigTextFieldTokens.TypeHighlightColor.toColor(),
    errorTypingHighlightColor: Color = HedvigTextFieldTokens.ErrorTypeHighlightColor.toColor(),
    cursorColor: Color = HedvigTextFieldTokens.CaretColor.toColor(),
    errorCursorColor: Color = HedvigTextFieldTokens.ErrorFocusCaretColor.toColor(),
    selectionColors: TextSelectionColors = LocalTextSelectionColors.current,
    focusedIndicatorColor: Color = HedvigTextFieldTokens.FocusActiveIndicatorColor.toColor(),
    unfocusedIndicatorColor: Color = HedvigTextFieldTokens.ActiveIndicatorColor.toColor(),
    disabledIndicatorColor: Color = HedvigTextFieldTokens.DisabledActiveIndicatorColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledActiveIndicatorOpacity),
    errorIndicatorColor: Color = HedvigTextFieldTokens.ErrorActiveIndicatorColor.toColor(),
    focusedLeadingIconColor: Color = HedvigTextFieldTokens.FocusLeadingIconColor.toColor(),
    unfocusedLeadingIconColor: Color = HedvigTextFieldTokens.LeadingIconColor.toColor(),
    disabledLeadingIconColor: Color = HedvigTextFieldTokens.DisabledLeadingIconColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledLeadingIconOpacity),
    errorLeadingIconColor: Color = HedvigTextFieldTokens.ErrorLeadingIconColor.toColor(),
    focusedTrailingIconColor: Color = HedvigTextFieldTokens.FocusTrailingIconColor.toColor(),
    unfocusedTrailingIconColor: Color = HedvigTextFieldTokens.TrailingIconColor.toColor(),
    disabledTrailingIconColor: Color = HedvigTextFieldTokens.DisabledTrailingIconColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledTrailingIconOpacity),
    errorTrailingIconColor: Color = HedvigTextFieldTokens.ErrorTrailingIconColor.toColor(),
    focusedLabelColor: Color = HedvigTextFieldTokens.FocusLabelColor.toColor(),
    unfocusedLabelColor: Color = HedvigTextFieldTokens.LabelColor.toColor(),
    disabledLabelColor: Color = HedvigTextFieldTokens.DisabledLabelColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledLabelOpacity),
    errorLabelColor: Color = HedvigTextFieldTokens.ErrorLabelColor.toColor(),
    focusedPlaceholderColor: Color = HedvigTextFieldTokens.InputPlaceholderColor.toColor(),
    unfocusedPlaceholderColor: Color = HedvigTextFieldTokens.InputPlaceholderColor.toColor(),
    disabledPlaceholderColor: Color = HedvigTextFieldTokens.DisabledInputColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledInputOpacity),
    errorPlaceholderColor: Color = HedvigTextFieldTokens.InputPlaceholderColor.toColor(),
    focusedSupportingTextColor: Color = HedvigTextFieldTokens.FocusSupportingColor.toColor(),
    unfocusedSupportingTextColor: Color = HedvigTextFieldTokens.SupportingColor.toColor(),
    disabledSupportingTextColor: Color = HedvigTextFieldTokens.DisabledSupportingColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledSupportingOpacity),
    errorSupportingTextColor: Color = HedvigTextFieldTokens.ErrorSupportingColor.toColor(),
    focusedPrefixColor: Color = HedvigTextFieldTokens.InputPrefixColor.toColor(),
    unfocusedPrefixColor: Color = HedvigTextFieldTokens.InputPrefixColor.toColor(),
    disabledPrefixColor: Color = HedvigTextFieldTokens.InputPrefixColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledInputOpacity),
    errorPrefixColor: Color = HedvigTextFieldTokens.InputPrefixColor.toColor(),
    focusedSuffixColor: Color = HedvigTextFieldTokens.InputSuffixColor.toColor(),
    unfocusedSuffixColor: Color = HedvigTextFieldTokens.InputSuffixColor.toColor(),
    disabledSuffixColor: Color = HedvigTextFieldTokens.InputSuffixColor.toColor()
      .copy(alpha = HedvigTextFieldTokens.DisabledInputOpacity),
    errorSuffixColor: Color = HedvigTextFieldTokens.InputSuffixColor.toColor(),
  ): HedvigTextFieldColors = HedvigTextFieldColors(
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    disabledTextColor = disabledTextColor,
    errorTextColor = errorTextColor,
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    disabledContainerColor = disabledContainerColor,
    errorContainerColor = errorContainerColor,
    typingHighlightColor = typingHighlightColor,
    errorTypingHighlightColor = errorTypingHighlightColor,
    cursorColor = cursorColor,
    errorCursorColor = errorCursorColor,
    textSelectionColors = selectionColors,
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor,
    disabledIndicatorColor = disabledIndicatorColor,
    errorIndicatorColor = errorIndicatorColor,
    focusedLeadingIconColor = focusedLeadingIconColor,
    unfocusedLeadingIconColor = unfocusedLeadingIconColor,
    disabledLeadingIconColor = disabledLeadingIconColor,
    errorLeadingIconColor = errorLeadingIconColor,
    focusedTrailingIconColor = focusedTrailingIconColor,
    unfocusedTrailingIconColor = unfocusedTrailingIconColor,
    disabledTrailingIconColor = disabledTrailingIconColor,
    errorTrailingIconColor = errorTrailingIconColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    disabledLabelColor = disabledLabelColor,
    errorLabelColor = errorLabelColor,
    focusedPlaceholderColor = focusedPlaceholderColor,
    unfocusedPlaceholderColor = unfocusedPlaceholderColor,
    disabledPlaceholderColor = disabledPlaceholderColor,
    errorPlaceholderColor = errorPlaceholderColor,
    focusedSupportingTextColor = focusedSupportingTextColor,
    unfocusedSupportingTextColor = unfocusedSupportingTextColor,
    disabledSupportingTextColor = disabledSupportingTextColor,
    errorSupportingTextColor = errorSupportingTextColor,
    focusedPrefixColor = focusedPrefixColor,
    unfocusedPrefixColor = unfocusedPrefixColor,
    disabledPrefixColor = disabledPrefixColor,
    errorPrefixColor = errorPrefixColor,
    focusedSuffixColor = focusedSuffixColor,
    unfocusedSuffixColor = unfocusedSuffixColor,
    disabledSuffixColor = disabledSuffixColor,
    errorSuffixColor = errorSuffixColor,
  )

  /**
   * A decoration box which helps creating custom text fields based on
   * <a href="https://material.io/components/text-fields#filled-text-field" class="external" target="_blank">Material Design filled text field</a>.
   *
   * If your text field requires customising elements that aren't exposed by [TextField],
   * consider using this decoration box to achieve the desired design.
   *
   * For example, if you need to create a dense text field, use [contentPadding] parameter to
   * decrease the paddings around the input field. If you need to customise the bottom indicator,
   * apply [borderLine] modifier to achieve that.
   *
   * See example of using [DecorationBox] to build your own custom text field
   * @sample androidx.compose.material3.samples.CustomTextFieldBasedOnDecorationBox
   *
   * @param value the input [String] shown by the text field
   * @param innerTextField input text field that this decoration box wraps. You will pass here a
   * framework-controlled composable parameter "innerTextField" from the decorationBox lambda of
   * the [BasicTextField]
   * @param enabled controls the enabled state of the text field. When `false`, this component
   * will not respond to user input, and it will appear visually disabled and disabled to
   * accessibility services. You must also pass the same value to the [BasicTextField] for it to
   * adjust the behavior accordingly.
   * @param singleLine indicates if this is a single line or multi line text field. You must pass
   * the same value as to [BasicTextField].
   * @param visualTransformation transforms the visual representation of the input [value]. You
   * must pass the same value as to [BasicTextField].
   * @param interactionSource the read-only [InteractionSource] representing the stream of
   * [Interaction]s for this text field. You must first create and pass in your own `remember`ed
   * [MutableInteractionSource] instance to the [BasicTextField] for it to dispatch events. And
   * then pass the same instance to this decoration box to observe [Interaction]s and customize
   * the appearance / behavior of this text field in different states.
   * @param isError indicates if the text field's current value is in error state. If set to
   * true, the label, bottom indicator and trailing icon by default will be displayed in error
   * color.
   * @param label the optional label to be displayed inside the text field container. The default
   * text style for internal [Text] is [Typography.bodySmall] when the text field is in focus and
   * [Typography.bodyLarge] when the text field is not in focus.
   * @param placeholder the optional placeholder to be displayed when the text field is in focus
   * and the input text is empty. The default text style for internal [Text] is
   * [Typography.bodyLarge].
   * @param leadingIcon the optional leading icon to be displayed at the beginning of the text
   * field container
   * @param trailingIcon the optional trailing icon to be displayed at the end of the text field
   * container
   * @param prefix the optional prefix to be displayed before the input text in the text field
   * @param suffix the optional suffix to be displayed after the input text in the text field
   * @param supportingText the optional supporting text to be displayed below the text field
   * @param shape defines the shape of this text field's container
   * @param colors [TextFieldColors] that will be used to resolve the colors used for this text
   * field in different states. See [TextFieldDefaults.colors].
   * @param contentPadding the spacing values to apply internally between the internals of text
   * field and the decoration box container. You can use it to implement dense text fields or
   * simply to control horizontal padding. See [TextFieldDefaults.contentPaddingWithLabel] and
   * [TextFieldDefaults.contentPaddingWithoutLabel].
   * Note that if there's a label in the text field, the [top][PaddingValues.calculateTopPadding]
   * padding represents the distance from the top edge of the container to the top of the label.
   * Otherwise if label is null, it represents the distance from the top edge of the container to
   * the top of the input field. All other paddings represent the distance from the corresponding
   * edge of the container to the corresponding edge of the closest element.
   * @param container the container to be drawn behind the text field. By default, this includes
   * the bottom indicator line. Default colors for the container come from the [colors].
   */
  @Composable
  fun DecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    enabled: Boolean,
    singleLine: Boolean,
    visualTransformation: VisualTransformation,
    interactionSource: InteractionSource,
    // Adapts the TextField to have the big card size and the bigger text size.
    withNewDesign: Boolean = false,
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    shape: Shape = HedvigTextFieldDefaults.shape,
    colors: HedvigTextFieldColors = colors(),
    contentPadding: PaddingValues =
      if (label == null) {
        contentPaddingWithoutLabel()
      } else {
        contentPaddingWithLabel()
      },
    container: @Composable () -> Unit = {
      ContainerBox(value, enabled, isError, interactionSource, colors, shape = shape)
    },
  ) {
    HedvigDecorationBox(
      value = value,
      innerTextField = innerTextField,
      visualTransformation = visualTransformation,
      placeholder = placeholder,
      label = label,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      prefix = prefix,
      suffix = suffix,
      supportingText = supportingText,
      withNewDesign = withNewDesign,
      singleLine = singleLine,
      enabled = enabled,
      isError = isError,
      interactionSource = interactionSource,
      colors = colors,
      contentPadding = contentPadding,
      container = container,
    )
  }
}

/**
 * Represents the colors of the input text, container, and content (including label, placeholder,
 * leading and trailing icons) used in a text field in different states.
 *
 * See [TextFieldDefaults.colors] for the default colors used in [TextField].
 * See [OutlinedTextFieldDefaults.colors] for the default colors used in [OutlinedTextField].
 */
@Immutable
class HedvigTextFieldColors internal constructor(
  private val focusedTextColor: Color,
  private val unfocusedTextColor: Color,
  private val disabledTextColor: Color,
  private val errorTextColor: Color,
  private val focusedContainerColor: Color,
  private val unfocusedContainerColor: Color,
  private val disabledContainerColor: Color,
  private val errorContainerColor: Color,
  // The pulsating color that the container will temporarily have on new text.
  private val typingHighlightColor: Color,
  // The pulsating color when there's an error too
  private val errorTypingHighlightColor: Color,
  private val cursorColor: Color,
  private val errorCursorColor: Color,
  private val textSelectionColors: TextSelectionColors,
  private val focusedIndicatorColor: Color,
  private val unfocusedIndicatorColor: Color,
  private val disabledIndicatorColor: Color,
  private val errorIndicatorColor: Color,
  private val focusedLeadingIconColor: Color,
  private val unfocusedLeadingIconColor: Color,
  private val disabledLeadingIconColor: Color,
  private val errorLeadingIconColor: Color,
  private val focusedTrailingIconColor: Color,
  private val unfocusedTrailingIconColor: Color,
  private val disabledTrailingIconColor: Color,
  private val errorTrailingIconColor: Color,
  private val focusedLabelColor: Color,
  private val unfocusedLabelColor: Color,
  private val disabledLabelColor: Color,
  private val errorLabelColor: Color,
  private val focusedPlaceholderColor: Color,
  private val unfocusedPlaceholderColor: Color,
  private val disabledPlaceholderColor: Color,
  private val errorPlaceholderColor: Color,
  private val focusedSupportingTextColor: Color,
  private val unfocusedSupportingTextColor: Color,
  private val disabledSupportingTextColor: Color,
  private val errorSupportingTextColor: Color,
  private val focusedPrefixColor: Color,
  private val unfocusedPrefixColor: Color,
  private val disabledPrefixColor: Color,
  private val errorPrefixColor: Color,
  private val focusedSuffixColor: Color,
  private val unfocusedSuffixColor: Color,
  private val disabledSuffixColor: Color,
  private val errorSuffixColor: Color,
) {
  /**
   * Represents the color used for the leading icon of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun leadingIconColor(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    return rememberUpdatedState(
      when {
        !enabled -> disabledLeadingIconColor
        isError -> errorLeadingIconColor
        focused -> focusedLeadingIconColor
        else -> unfocusedLeadingIconColor
      },
    )
  }

  /**
   * Represents the color used for the trailing icon of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun trailingIconColor(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    return rememberUpdatedState(
      when {
        !enabled -> disabledTrailingIconColor
        isError -> errorTrailingIconColor
        focused -> focusedTrailingIconColor
        else -> unfocusedTrailingIconColor
      },
    )
  }

  /**
   * Represents the color used for the border indicator of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun indicatorColor(enabled: Boolean, isError: Boolean, interactionSource: InteractionSource): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledIndicatorColor
      isError -> errorIndicatorColor
      focused -> focusedIndicatorColor
      else -> unfocusedIndicatorColor
    }
    return if (enabled) {
      animateColorAsState(targetValue, tween(durationMillis = AnimationDuration))
    } else {
      rememberUpdatedState(targetValue)
    }
  }

  /**
   * Represents the container color for this text field.
   *
   * @param value the value of the textfield. Used to know when it changes, to animate the color changes.
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun containerColor(
    value: String,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    var gotFreshValue by remember { mutableStateOf(false) }
    val updatedValue by rememberUpdatedState(value)
    LaunchedEffect(Unit) {
      snapshotFlow { updatedValue }
        .drop(1)
        .collectLatest {
          gotFreshValue = true
          delay(SignalAnimationDuration)
          gotFreshValue = false
        }
    }
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      gotFreshValue && isError -> errorTypingHighlightColor
      gotFreshValue -> typingHighlightColor
      !enabled -> disabledContainerColor
      isError -> errorContainerColor
      focused -> focusedContainerColor
      else -> unfocusedContainerColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = SignalAnimationDuration.toInt(),
      ),
    )
  }

  /**
   * Represents the color used for the placeholder of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun placeholderColor(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledPlaceholderColor
      isError -> errorPlaceholderColor
      focused -> focusedPlaceholderColor
      else -> unfocusedPlaceholderColor
    }
    return rememberUpdatedState(targetValue)
  }

  /**
   * Represents the color used for the label of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun labelColor(enabled: Boolean, isError: Boolean, interactionSource: InteractionSource): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledLabelColor
      isError -> errorLabelColor
      focused -> focusedLabelColor
      else -> unfocusedLabelColor
    }
    return rememberUpdatedState(targetValue)
  }

  /**
   * Represents the color used for the input field of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun textColor(enabled: Boolean, isError: Boolean, interactionSource: InteractionSource): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledTextColor
      isError -> errorTextColor
      focused -> focusedTextColor
      else -> unfocusedTextColor
    }
    return rememberUpdatedState(targetValue)
  }

  @Composable
  internal fun supportingTextColor(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
  ): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    return rememberUpdatedState(
      when {
        !enabled -> disabledSupportingTextColor
        isError -> errorSupportingTextColor
        focused -> focusedSupportingTextColor
        else -> unfocusedSupportingTextColor
      },
    )
  }

  /**
   * Represents the color used for the prefix of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun prefixColor(enabled: Boolean, isError: Boolean, interactionSource: InteractionSource): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledPrefixColor
      isError -> errorPrefixColor
      focused -> focusedPrefixColor
      else -> unfocusedPrefixColor
    }
    return rememberUpdatedState(targetValue)
  }

  /**
   * Represents the color used for the suffix of this text field.
   *
   * @param enabled whether the text field is enabled
   * @param isError whether the text field's current value is in error
   * @param interactionSource the [InteractionSource] of this text field. Helps to determine if
   * the text field is in focus or not
   */
  @Composable
  internal fun suffixColor(enabled: Boolean, isError: Boolean, interactionSource: InteractionSource): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
      !enabled -> disabledSuffixColor
      isError -> errorSuffixColor
      focused -> focusedSuffixColor
      else -> unfocusedSuffixColor
    }
    return rememberUpdatedState(targetValue)
  }

  /**
   * Represents the color used for the cursor of this text field.
   *
   * @param isError whether the text field's current value is in error
   */
  @Composable
  internal fun cursorColor(isError: Boolean): State<Color> {
    return rememberUpdatedState(if (isError) errorCursorColor else cursorColor)
  }

  /**
   * Represents the colors used for text selection in this text field.
   */
  internal val selectionColors: TextSelectionColors
    @Composable get() = textSelectionColors

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is HedvigTextFieldColors) return false

    if (focusedTextColor != other.focusedTextColor) return false
    if (unfocusedTextColor != other.unfocusedTextColor) return false
    if (disabledTextColor != other.disabledTextColor) return false
    if (errorTextColor != other.errorTextColor) return false
    if (focusedContainerColor != other.focusedContainerColor) return false
    if (unfocusedContainerColor != other.unfocusedContainerColor) return false
    if (disabledContainerColor != other.disabledContainerColor) return false
    if (errorContainerColor != other.errorContainerColor) return false
    if (typingHighlightColor != other.typingHighlightColor) return false
    if (errorTypingHighlightColor != other.errorTypingHighlightColor) return false
    if (cursorColor != other.cursorColor) return false
    if (errorCursorColor != other.errorCursorColor) return false
    if (textSelectionColors != other.textSelectionColors) return false
    if (focusedIndicatorColor != other.focusedIndicatorColor) return false
    if (unfocusedIndicatorColor != other.unfocusedIndicatorColor) return false
    if (disabledIndicatorColor != other.disabledIndicatorColor) return false
    if (errorIndicatorColor != other.errorIndicatorColor) return false
    if (focusedLeadingIconColor != other.focusedLeadingIconColor) return false
    if (unfocusedLeadingIconColor != other.unfocusedLeadingIconColor) return false
    if (disabledLeadingIconColor != other.disabledLeadingIconColor) return false
    if (errorLeadingIconColor != other.errorLeadingIconColor) return false
    if (focusedTrailingIconColor != other.focusedTrailingIconColor) return false
    if (unfocusedTrailingIconColor != other.unfocusedTrailingIconColor) return false
    if (disabledTrailingIconColor != other.disabledTrailingIconColor) return false
    if (errorTrailingIconColor != other.errorTrailingIconColor) return false
    if (focusedLabelColor != other.focusedLabelColor) return false
    if (unfocusedLabelColor != other.unfocusedLabelColor) return false
    if (disabledLabelColor != other.disabledLabelColor) return false
    if (errorLabelColor != other.errorLabelColor) return false
    if (focusedPlaceholderColor != other.focusedPlaceholderColor) return false
    if (unfocusedPlaceholderColor != other.unfocusedPlaceholderColor) return false
    if (disabledPlaceholderColor != other.disabledPlaceholderColor) return false
    if (errorPlaceholderColor != other.errorPlaceholderColor) return false
    if (focusedSupportingTextColor != other.focusedSupportingTextColor) return false
    if (unfocusedSupportingTextColor != other.unfocusedSupportingTextColor) return false
    if (disabledSupportingTextColor != other.disabledSupportingTextColor) return false
    if (errorSupportingTextColor != other.errorSupportingTextColor) return false
    if (focusedPrefixColor != other.focusedPrefixColor) return false
    if (unfocusedPrefixColor != other.unfocusedPrefixColor) return false
    if (disabledPrefixColor != other.disabledPrefixColor) return false
    if (errorPrefixColor != other.errorPrefixColor) return false
    if (focusedSuffixColor != other.focusedSuffixColor) return false
    if (unfocusedSuffixColor != other.unfocusedSuffixColor) return false
    if (disabledSuffixColor != other.disabledSuffixColor) return false
    if (errorSuffixColor != other.errorSuffixColor) return false

    return true
  }

  override fun hashCode(): Int {
    var result = focusedTextColor.hashCode()
    result = 31 * result + unfocusedTextColor.hashCode()
    result = 31 * result + disabledTextColor.hashCode()
    result = 31 * result + errorTextColor.hashCode()
    result = 31 * result + focusedContainerColor.hashCode()
    result = 31 * result + unfocusedContainerColor.hashCode()
    result = 31 * result + disabledContainerColor.hashCode()
    result = 31 * result + errorContainerColor.hashCode()
    result = 31 * result + typingHighlightColor.hashCode()
    result = 31 * result + errorTypingHighlightColor.hashCode()
    result = 31 * result + cursorColor.hashCode()
    result = 31 * result + errorCursorColor.hashCode()
    result = 31 * result + textSelectionColors.hashCode()
    result = 31 * result + focusedIndicatorColor.hashCode()
    result = 31 * result + unfocusedIndicatorColor.hashCode()
    result = 31 * result + disabledIndicatorColor.hashCode()
    result = 31 * result + errorIndicatorColor.hashCode()
    result = 31 * result + focusedLeadingIconColor.hashCode()
    result = 31 * result + unfocusedLeadingIconColor.hashCode()
    result = 31 * result + disabledLeadingIconColor.hashCode()
    result = 31 * result + errorLeadingIconColor.hashCode()
    result = 31 * result + focusedTrailingIconColor.hashCode()
    result = 31 * result + unfocusedTrailingIconColor.hashCode()
    result = 31 * result + disabledTrailingIconColor.hashCode()
    result = 31 * result + errorTrailingIconColor.hashCode()
    result = 31 * result + focusedLabelColor.hashCode()
    result = 31 * result + unfocusedLabelColor.hashCode()
    result = 31 * result + disabledLabelColor.hashCode()
    result = 31 * result + errorLabelColor.hashCode()
    result = 31 * result + focusedPlaceholderColor.hashCode()
    result = 31 * result + unfocusedPlaceholderColor.hashCode()
    result = 31 * result + disabledPlaceholderColor.hashCode()
    result = 31 * result + errorPlaceholderColor.hashCode()
    result = 31 * result + focusedSupportingTextColor.hashCode()
    result = 31 * result + unfocusedSupportingTextColor.hashCode()
    result = 31 * result + disabledSupportingTextColor.hashCode()
    result = 31 * result + errorSupportingTextColor.hashCode()
    result = 31 * result + focusedPrefixColor.hashCode()
    result = 31 * result + unfocusedPrefixColor.hashCode()
    result = 31 * result + disabledPrefixColor.hashCode()
    result = 31 * result + errorPrefixColor.hashCode()
    result = 31 * result + focusedSuffixColor.hashCode()
    result = 31 * result + unfocusedSuffixColor.hashCode()
    result = 31 * result + disabledSuffixColor.hashCode()
    result = 31 * result + errorSuffixColor.hashCode()
    return result
  }
}

@Composable
private fun animateBorderStrokeAsState(
  enabled: Boolean,
  isError: Boolean,
  interactionSource: InteractionSource,
  colors: HedvigTextFieldColors,
  focusedBorderThickness: Dp,
  unfocusedBorderThickness: Dp,
): State<BorderStroke> {
  val focused by interactionSource.collectIsFocusedAsState()
  val indicatorColor = colors.indicatorColor(enabled, isError, interactionSource)
  val targetThickness = if (focused) focusedBorderThickness else unfocusedBorderThickness
  val animatedThickness = if (enabled) {
    animateDpAsState(targetThickness, tween(durationMillis = AnimationDuration))
  } else {
    rememberUpdatedState(unfocusedBorderThickness)
  }
  return rememberUpdatedState(
    BorderStroke(animatedThickness.value, SolidColor(indicatorColor.value)),
  )
}
