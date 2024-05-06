package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextFieldColors
import com.hedvig.android.design.system.hedvig.HedvigTextFieldConfiguration
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.colors
import com.hedvig.android.design.system.hedvig.configuration
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Image
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalSharedTransitionApi::class)
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
  enabled: Boolean = true,
  isError: Boolean = false,
  readOnly: Boolean = false,
  interactionSource: InteractionSource,
) {
  val transformedText = remember(value, visualTransformation) {
    visualTransformation.filter(AnnotatedString(value))
  }.text.text

  val isFocused = interactionSource.collectIsFocusedAsState().value
  val inputPhase = when {
    isFocused -> InputPhase.Focused
    transformedText.isEmpty() -> InputPhase.UnfocusedEmpty
    else -> InputPhase.UnfocusedNotEmpty
  }

  val decoratedLabel: (@Composable (InputPhase) -> Unit)? = if (label != null) {
    @Suppress("NAME_SHADOWING")
    @Composable { inputPhase ->
      Decoration(
        colors.labelColor(enabled = enabled, isError = isError, value = value).value,
        if (inputPhase == InputPhase.UnfocusedEmpty) size.textStyle else size.labelTextStyle,
      ) { label() }
    }
  } else {
    null
  }
  val decoratedLeadingIcon: (@Composable () -> Unit)? = if (leadingIcon != null) {
    @Composable {
      Decoration(
        colors.trailingIconColor(readOnly = readOnly, enabled = enabled, isError = isError).value,
      ) { leadingIcon() }
    }
  } else {
    null
  }
  val decoratedTrailingIcon: (@Composable () -> Unit)? = if (trailingIcon != null) {
    @Composable {
      Decoration(
        colors.trailingIconColor(readOnly = readOnly, enabled = enabled, isError = isError).value,
      ) { trailingIcon() }
    }
  } else {
    null
  }
  val decoratedSupportingText: (@Composable () -> Unit)? = if (supportingText != null) {
    @Composable {
      Decoration(
        colors.supportingTextColor(),
        size.labelTextStyle,
      ) { supportingText() }
    }
  } else {
    null
  }

  Column {
    AnimatedTextFieldContent(
      inputPhase = inputPhase,
      configuration = configuration,
      size = size,
      innerTextField = innerTextField,
      label = decoratedLabel,
      leadingIcon = decoratedLeadingIcon,
      trailingIcon = decoratedTrailingIcon,
    ) { modifier, containerContent ->
      ContainerBox(
        value = value,
        isError = isError,
        colors = colors,
        configuration = configuration,
        modifier = modifier,
        content = containerContent,
      )
    }
    AnimatedContent(
      targetState = decoratedSupportingText,
      contentAlignment = Alignment.TopStart,
      transitionSpec = { expandVertically() togetherWith shrinkVertically() },
    ) { supportingText ->
      if (supportingText != null) {
        Box(Modifier.padding(size.supportingTextPadding)) {
          supportingText()
        }
      }
    }
  }
}

@Composable
private fun ContainerBox(
  value: String,
  isError: Boolean,
  colors: HedvigTextFieldColors,
  configuration: HedvigTextFieldConfiguration,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier.background(colors.containerColor(value, isError).value, configuration.shape),
  ) {
    content()
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedTextFieldContent(
  inputPhase: InputPhase,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  innerTextField: @Composable () -> Unit,
  label: @Composable ((InputPhase) -> Unit)?,
  leadingIcon: @Composable (() -> Unit)?,
  trailingIcon: @Composable (() -> Unit)?,
  container: @Composable (Modifier, @Composable BoxScope.() -> Unit) -> Unit,
) {
  SharedTransitionLayout {
    AnimatedContent(
      inputPhase,
      transitionSpec = {
        EnterTransition.None togetherWith ExitTransition.None
      },
    ) { inputPhase ->
      val sharedInnerTextField: @Composable (Modifier) -> Unit = { modifier ->
        Box(
          modifier.sharedElement(
            state = rememberSharedContentState(InnerTextFieldId),
            animatedVisibilityScope = this,
            boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
          ),
        ) { innerTextField() }
      }
      val sharedLabel: (@Composable () -> Unit)? = if (label != null) {
        @Composable {
          Box(
            Modifier.sharedBounds(
              sharedContentState = rememberSharedContentState(LabelId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
            ),
          ) {
            label(inputPhase)
          }
        }
      } else {
        null
      }

      val sharedLeadingIcon: (@Composable () -> Unit)? = if (leadingIcon != null) {
        @Composable {
          Box(
            Modifier.sharedElement(
              state = rememberSharedContentState(LeadingIconId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
              renderInOverlayDuringTransition = false,
            ),
          ) {
            leadingIcon()
          }
        }
      } else {
        null
      }

      val sharedTrailingIcon: (@Composable () -> Unit)? = if (trailingIcon != null) {
        @Composable {
          Box(
            Modifier.sharedElement(
              state = rememberSharedContentState(TrailingIconId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
              renderInOverlayDuringTransition = false,
            ),
          ) {
            trailingIcon()
          }
        }
      } else {
        null
      }
      container(
        Modifier.sharedElement(
          state = rememberSharedContentState(ContainerId),
          animatedVisibilityScope = this,
          boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
        ),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(size.horizontalPadding()),
        ) {
          val textContentPadding = size.textAndLabelVerticalPadding(inputPhase.onlyShowLabel)
          if (sharedLeadingIcon != null) {
            sharedLeadingIcon.invoke()
            Spacer(Modifier.width(configuration.iconToTextPadding))
          }
          Column {
            Spacer(Modifier.height(textContentPadding.calculateTopPadding()))
            if (inputPhase.onlyShowLabel) {
              sharedLabel?.invoke()
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(-size.labelToTextOverlap)) {
                sharedLabel?.invoke()
                sharedInnerTextField(Modifier)
              }
            }
            Spacer(Modifier.height(textContentPadding.calculateBottomPadding()))
            if (inputPhase.onlyShowLabel) {
              // Lay out the text at the *bottom* of the container, so it starts animating in from there towards its
              // final position.
              sharedInnerTextField(
                Modifier
                  .requiredHeight(0.dp)
                  .wrapContentHeight(Alignment.Bottom, unbounded = true),
              )
            }
          }
          if (sharedTrailingIcon != null) {
            Spacer(Modifier.width(configuration.iconToTextPadding))
            sharedTrailingIcon.invoke()
          }
        }
      }
    }
  }
}

internal enum class InputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty,
  ;

  val onlyShowLabel: Boolean
    get() = this == UnfocusedEmpty
}

@Preview
@Composable
private fun PreviewHedvigDecorationBox(
  @PreviewParameter(PreviewTextFieldInput::class) previewTextFieldInputState: PreviewTextFieldInputState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val value = "Text field".takeIf { !previewTextFieldInputState.isEmpty }.orEmpty()
      HedvigDecorationBox(
        value = value,
        colors = HedvigTextFieldDefaults.colors(),
        configuration = HedvigTextFieldDefaults.configuration(),
        size = HedvigTextFieldSize.Large,
        innerTextField = { BasicTextField(value = value, onValueChange = {}) },
        visualTransformation = VisualTransformation.None,
        label = { HedvigText(text = "Label") },
        leadingIcon = { Icon(HedvigIcons.Image, null) },
        trailingIcon = { Icon(HedvigIcons.Image, null) },
        interactionSource = remember {
          if (previewTextFieldInputState.isFocused) {
            object : MutableInteractionSource {
              override val interactions: Flow<Interaction>
                get() = flowOf(HoverInteraction.Enter())

              override suspend fun emit(interaction: Interaction) {
              }

              override fun tryEmit(interaction: Interaction): Boolean {
                return false
              }
            }
          } else {
            MutableInteractionSource()
          }
        },
      )
    }
  }
}

private class PreviewTextFieldInput : CollectionPreviewParameterProvider<PreviewTextFieldInputState>(
  PreviewTextFieldInputState.entries.toList(),
)

private enum class PreviewTextFieldInputState {
  Focused,
  FocusedEmpty,
  Unfocused,
  UnfocusedEmpty,
  ;

  val isFocused: Boolean
    get() = this == PreviewTextFieldInputState.Focused || this == PreviewTextFieldInputState.FocusedEmpty

  val isEmpty: Boolean
    get() = this == PreviewTextFieldInputState.FocusedEmpty || this == PreviewTextFieldInputState.UnfocusedEmpty
}

private const val InnerTextFieldId = "InnerTextField"
private const val LabelId = "Label"
private const val LeadingIconId = "LeadingIcon"
private const val TrailingIconId = "TrailingIcon"
private const val ContainerId = "Container"

private const val TextFieldLabelAnimationDuration = 150
private val LabelTransitionAnimationSpec: FiniteAnimationSpec<Rect> = tween<Rect>(TextFieldLabelAnimationDuration)
