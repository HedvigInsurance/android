package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
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
  singleLine: Boolean = false,
  enabled: Boolean = true,
  isError: Boolean = false,
  interactionSource: InteractionSource,
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

  val decoratedInnerTextField: @Composable () -> Unit = {
    Decoration(
      colors.textColor(value = value, enabled = enabled, isError = isError).value,
      size.textStyle,
    ) {
      innerTextField()
    }
  }

  val decoratedLabel: (@Composable () -> Unit)? = if (label != null) {
    @Composable {
      Decoration(
        colors.labelColor(enabled = enabled, isError = isError, value = value).value,
        size.labelTextStyle,
      ) { label() }
    }
  } else {
    null
  }
  val decoratedLeadingIcon: (@Composable () -> Unit)? = if (leadingIcon != null) {
    @Composable {
      Decoration(
        colors.trailingIconColor(enabled = enabled, isError = isError, interactionSource = interactionSource).value,
      ) { leadingIcon() }
    }
  } else {
    null
  }
  val decoratedTrailingIcon: (@Composable () -> Unit)? = if (trailingIcon != null) {
    @Composable {
      Decoration(
        colors.trailingIconColor(enabled = enabled, isError = isError, interactionSource = interactionSource).value,
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
    SharedTransitionLayout {
      AnimatedContent(
        inputState,
        transitionSpec = {
          EnterTransition.None togetherWith ExitTransition.None
        },
      ) { inputPhase ->
        this@SharedTransitionLayout.TextFieldContent(
          this,
          inputPhase,
          value,
          configuration,
          size,
          decoratedInnerTextField,
          decoratedLabel,
          decoratedLeadingIcon,
          decoratedTrailingIcon,
          isFocused,
        ) { modifier, containerContent ->
          ContainerBox(
            value = value,
            isError = isError,
            colors = colors,
            size = size,
            configuration = configuration,
            modifier = modifier,
            content = containerContent,
          )
        }
      }
    }
    AnimatedContent(
      targetState = decoratedSupportingText,
      contentAlignment = Alignment.TopCenter,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.TextFieldContent(
  animatedContentScope: AnimatedContentScope,
  inputPhase: InputPhase,
  value: String,
  configuration: HedvigTextFieldConfiguration,
  size: HedvigTextFieldSize,
  innerTextField: @Composable () -> Unit,
  label: @Composable (() -> Unit)?,
  leadingIcon: @Composable (() -> Unit)?,
  trailingIcon: @Composable (() -> Unit)?,
  isFocused: Boolean,
  container: @Composable (Modifier, @Composable BoxScope.() -> Unit) -> Unit,
) {
  val sharedInnerTextField: @Composable () -> Unit = {
    Box(Modifier.sharedElement(rememberSharedContentState("innerTextField"), animatedContentScope)) { innerTextField() }
  }
  val sharedLabel: (@Composable () -> Unit)? = if (label != null) {
    @Composable {
      Box(Modifier.sharedBounds(rememberSharedContentState("label"), animatedContentScope)) { label() }
    }
  } else {
    null
  }

  val sharedLeadingIcon: (@Composable () -> Unit)? = if (leadingIcon != null) {
    @Composable {
      Box(Modifier.sharedElement(rememberSharedContentState("leadingIcon"), animatedContentScope)) { leadingIcon() }
    }
  } else {
    null
  }

  val sharedTrailingIcon: (@Composable () -> Unit)? = if (trailingIcon != null) {
    @Composable {
      Box(
        Modifier.sharedElement(rememberSharedContentState("sharedTrailingIcon"), animatedContentScope),
      ) { trailingIcon() }
    }
  } else {
    null
  }
  container(
    Modifier.sharedElement(
      state = rememberSharedContentState("container"),
      animatedVisibilityScope = animatedContentScope,
      renderInOverlayDuringTransition = false,
    ),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(size.contentPadding(value = value, isFocused = isFocused)),
    ) {
      if (sharedLeadingIcon != null) {
        sharedLeadingIcon.invoke()
        Spacer(Modifier.width(configuration.iconToTextPadding))
      }
      when (inputPhase) {
        InputPhase.Focused,
        InputPhase.UnfocusedNotEmpty,
        -> {
          Column(verticalArrangement = Arrangement.spacedBy(-size.labelToTextOverlap)) {
            sharedLabel?.invoke()
            sharedInnerTextField()
          }
        }

        InputPhase.UnfocusedEmpty -> {
          sharedLabel?.invoke()
        }
      }
      if (sharedTrailingIcon != null) {
        Spacer(Modifier.width(configuration.iconToTextPadding))
        sharedTrailingIcon.invoke()
      }
    }
  }
}

@Composable
private fun ContainerBox(
  value: String,
  isError: Boolean,
  colors: HedvigTextFieldColors,
  size: HedvigTextFieldSize,
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

private enum class InputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty,
}

internal const val SignalAnimationDuration = 400L
internal const val TextFieldLabelAnimationDuration = 150

@Preview
@Composable
private fun PreviewHedvigDecorationBox() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        PreviewTextFieldInputState.entries.toList(),
      ) { state ->
        val value = "Text field".takeIf { !state.isEmpty }.orEmpty()
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
            if (state.isFocused) {
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
}

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
