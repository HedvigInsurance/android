package com.hedvig.android.design.system.hedvig

import android.annotation.SuppressLint
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
internal fun HedvigDecorationBox(
  value: String,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  innerTextField: @Composable () -> Unit,
  visualTransformation: VisualTransformation,
  label: @Composable (() -> Unit)?,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  singleLine: Boolean = false,
  enabled: Boolean = true,
  isError: Boolean = false,
  interactionSource: InteractionSource,
  contentPadding: PaddingValues,
  container: @Composable () -> Unit,
) {
  val transformedText = remember(value, visualTransformation) {
    visualTransformation.filter(AnnotatedString(value))
  }.text.text

  val isFocused = interactionSource.collectIsFocusedAsState().value
  val inputState = when {
    isFocused -> InputPhase.Focused
    transformedText.isEmpty() -> InputPhase.UnfocusedEmpty
    else -> InputPhase.UnfocusedNotEmpty
  }

  val labelColor = colors.labelColor(value, enabled, isError).value
  val labelBigTypography = size.textStyle
  val labelSmallTypography = size.labelTextStyle

  TextFieldTransitionScope.Transition(
    inputState = inputState,
    focusedTextStyleColor = labelColor,
    unfocusedTextStyleColor = labelColor,
    contentColor = labelColor,
    showLabel = label != null,
  ) { labelProgress, labelTextStyleColor, labelContentColor ->
    val decoratedLabel: @Composable (() -> Unit)? = label?.let {
      @Composable {
        val labelTextStyle = lerp(
          start = labelBigTypography,
          stop = labelSmallTypography,
          fraction = labelProgress,
        ).let {
          it.copy(color = labelTextStyleColor)
        }
        Decoration(labelContentColor, labelTextStyle, it)
      }
    }

    // Developers need to handle invalid input manually. But since we don't provide error
    // message slot API, we can set the default error message in case developers forget about
    // it.
    @SuppressLint("PrivateResource")
    val defaultErrorMessage = stringResource(androidx.compose.ui.R.string.default_error_message)
    val decorationBoxModifier = Modifier.semantics { if (isError) error(defaultErrorMessage) }

    // todo add leading icon color.
    val leadingIconColor = colors.trailingIconColor(enabled, isError, interactionSource).value
    val decoratedLeading: @Composable (() -> Unit)? = leadingIcon?.let {
      @Composable {
        Decoration(contentColor = leadingIconColor, content = it)
      }
    }

    val trailingIconColor = colors.trailingIconColor(enabled, isError, interactionSource).value
    val decoratedTrailing: @Composable (() -> Unit)? = trailingIcon?.let {
      @Composable {
        Decoration(contentColor = trailingIconColor, content = it)
      }
    }

    val supportingTextColor = colors.supportingTextColor()
    val decoratedSupporting: @Composable (() -> Unit)? = supportingText?.let {
      @Composable {
        Decoration(
          contentColor = supportingTextColor,
          typography = configuration.supportingTextStyle,
          content = it,
        )
      }
    }

    val containerWithId: @Composable () -> Unit = {
      Box(
        Modifier.layoutId(ContainerId),
        propagateMinConstraints = true,
      ) {
        container()
      }
    }

    HedvigTextFieldLayout(
      modifier = decorationBoxModifier,
      size = size,
      textField = innerTextField,
      label = decoratedLabel,
      leading = decoratedLeading,
      trailing = decoratedTrailing,
      container = containerWithId,
      supporting = decoratedSupporting,
      singleLine = singleLine,
      animationProgress = labelProgress,
      paddingValues = contentPadding,
    )
  }
}

/**
 * Set content color, typography and emphasis for [content] composable
 */
@Composable
internal fun Decoration(contentColor: Color, typography: TextStyle? = null, content: @Composable () -> Unit) {
  val contentWithColor: @Composable () -> Unit = @Composable {
    CompositionLocalProvider(
      LocalContentColor provides contentColor,
      content = content,
    )
  }
  if (typography != null) ProvideTextStyle(typography, contentWithColor) else contentWithColor()
}

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0

internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

private object TextFieldTransitionScope {
  @Composable
  fun Transition(
    inputState: InputPhase,
    focusedTextStyleColor: Color,
    unfocusedTextStyleColor: Color,
    contentColor: Color,
    showLabel: Boolean,
    content: @Composable (
      labelProgress: Float,
      labelTextStyleColor: Color,
      labelContentColor: Color,
    ) -> Unit,
  ) {
    // Transitions from/to InputPhase.Focused are the most critical in the transition below.
    // UnfocusedEmpty <-> UnfocusedNotEmpty are needed when a single state is used to control
    // multiple text fields.
    val transition = updateTransition(inputState, label = "TextFieldInputState")

    val labelProgress by transition.animateFloat(
      label = "LabelProgress",
      transitionSpec = { tween(durationMillis = AnimationDuration) },
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> 0f
        InputPhase.UnfocusedNotEmpty -> 1f
      }
    }

    val labelTextStyleColor by transition.animateColor(
      transitionSpec = { tween(durationMillis = AnimationDuration) },
      label = "LabelTextStyleColor",
    ) {
      when (it) {
        InputPhase.Focused -> focusedTextStyleColor
        else -> unfocusedTextStyleColor
      }
    }

    content(
      labelProgress,
      labelTextStyleColor,
      contentColor,
    )
  }
}

/**
 * An internal state used to animate a label and an indicator.
 */
private enum class InputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty,
}

internal val IntrinsicMeasurable.layoutId: Any?
  get() = (parentData as? LayoutIdParentData)?.layoutId

internal const val TextFieldId = "TextField"
internal const val LabelId = "Label"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"
internal const val SupportingId = "Supporting"
internal const val ContainerId = "Container"
internal val ZeroConstraints = Constraints(0, 0, 0, 0)

internal const val SignalAnimationDuration = 400L
internal const val AnimationDuration = 150

internal val TextFieldPadding = 16.dp
internal val HorizontalIconPadding = 12.dp
internal val MinTextLineHeight = 24.dp
internal val MinFocusedLabelLineHeight = 16.dp

internal val IconDefaultSizeModifier = Modifier.defaultMinSize(48.dp, 48.dp)
