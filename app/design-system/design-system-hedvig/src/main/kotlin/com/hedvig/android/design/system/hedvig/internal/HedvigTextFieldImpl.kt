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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Constraints
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
import kotlin.math.max
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
  suffix: @Composable (() -> Unit)? = null,
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
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
        if (inputPhase.onlyShowLabel) size.textStyle else size.labelTextStyle,
      ) { label() }
    }
  } else {
    null
  }
  val decoratedSuffix: (@Composable (InputPhase) -> Unit)? = if (suffix != null) {
    @Suppress("NAME_SHADOWING")
    @Composable { inputPhase ->
      Decoration(
        if (inputPhase.onlyShowLabel) {
          colors.labelColor(value, enabled, isError).value
        } else {
          colors.textColor(value, enabled, isError).value
        },
        size.textStyle,
      ) { suffix() }
    }
  } else {
    null
  }
  val decoratedLeadingContent: (@Composable () -> Unit)? = if (leadingContent != null) {
    @Composable {
      Decoration(
        colors.trailingContentColor(readOnly = readOnly, enabled = enabled, isError = isError).value,
      ) { leadingContent() }
    }
  } else {
    null
  }
  val decoratedTrailingContent: (@Composable () -> Unit)? = if (trailingContent != null) {
    @Composable {
      Decoration(
        colors.trailingContentColor(readOnly = readOnly, enabled = enabled, isError = isError).value,
      ) { trailingContent() }
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
      suffix = decoratedSuffix,
      leadingContent = decoratedLeadingContent,
      trailingContent = decoratedTrailingContent,
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
  suffix: @Composable ((InputPhase) -> Unit)?,
  leadingContent: @Composable (() -> Unit)?,
  trailingContent: @Composable (() -> Unit)?,
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
      val sharedLabel: (@Composable (Modifier) -> Unit)? = if (label != null) {
        @Composable { modifier ->
          Box(
            modifier.sharedBounds(
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
      val sharedSuffix: (@Composable (Modifier) -> Unit)? = if (suffix != null) {
        @Composable { modifier ->
          Box(
            modifier.sharedBounds(
              sharedContentState = rememberSharedContentState(SuffixId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
            ),
          ) {
            suffix(inputPhase)
          }
        }
      } else {
        null
      }

      val sharedLeadingContent: (@Composable () -> Unit)? = if (leadingContent != null) {
        @Composable {
          Box(
            Modifier.sharedElement(
              state = rememberSharedContentState(LeadingContentId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
              renderInOverlayDuringTransition = false,
            ),
          ) {
            leadingContent()
          }
        }
      } else {
        null
      }

      val sharedTrailingContent: (@Composable () -> Unit)? = if (trailingContent != null) {
        @Composable {
          Box(
            Modifier.sharedElement(
              state = rememberSharedContentState(TrailingContentId),
              animatedVisibilityScope = this,
              boundsTransform = BoundsTransform { _, _ -> LabelTransitionAnimationSpec },
              renderInOverlayDuringTransition = false,
            ),
          ) {
            trailingContent()
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
        SharedTextFieldContent(
          size,
          inputPhase,
          configuration,
          sharedLabel,
          sharedSuffix,
          sharedInnerTextField,
          sharedLeadingContent,
          sharedTrailingContent,
        )
      }
    }
  }
}

@Composable
private fun SharedTextFieldContent(
  size: HedvigTextFieldSize,
  inputPhase: InputPhase,
  configuration: HedvigTextFieldConfiguration,
  label: @Composable() ((Modifier) -> Unit)?,
  suffix: @Composable() ((Modifier) -> Unit)?,
  innerTextField: @Composable (Modifier) -> Unit,
  leadingContent: @Composable() (() -> Unit)?,
  trailingContent: @Composable() (() -> Unit)?,
) {
  val centerContent: @Composable () -> Unit = {
    val textContentPadding = size.textAndLabelVerticalPadding(inputPhase.onlyShowLabel)
    if (inputPhase.onlyShowLabel) {
      Column {
        Spacer(Modifier.height(textContentPadding.calculateTopPadding()))
        Row(Modifier.fillMaxWidth()) {
          label?.invoke(Modifier)
          Spacer(Modifier.weight(1f).width(configuration.textFieldToOtherContentHorizontalPadding))
          suffix?.invoke(Modifier)
        }
        Spacer(Modifier.height(textContentPadding.calculateBottomPadding()))
        // Lay out the text at the *bottom* of the container, so it starts animating in from there towards its
        // final position.
        innerTextField(
          Modifier
            .requiredHeight(0.dp)
            .wrapContentHeight(Alignment.Bottom, unbounded = true),
        )
      }
    } else {
      Column {
        Spacer(Modifier.height(textContentPadding.calculateTopPadding()))
        Column(verticalArrangement = Arrangement.spacedBy(-size.labelToTextOverlap)) {
          label?.invoke(Modifier)
          Row(modifier = Modifier.fillMaxWidth()) {
            innerTextField(Modifier.weight(1f))
            Spacer(Modifier.width(configuration.textFieldToOtherContentHorizontalPadding))
            suffix?.invoke(Modifier)
          }
        }
        Spacer(Modifier.height(textContentPadding.calculateBottomPadding()))
      }
    }
  }
  RowTextFieldLayout(
    modifier = Modifier.padding(size.horizontalPadding()),
    centerContent = centerContent,
    leading = if (leadingContent != null) {
      {
        Row {
          leadingContent.invoke()
          Spacer(Modifier.width(configuration.textFieldToOtherContentHorizontalPadding))
        }
      }
    } else {
      null
    },
    trailing = if (trailingContent != null) {
      {
        Row {
          Spacer(Modifier.width(configuration.textFieldToOtherContentHorizontalPadding))
          trailingContent.invoke()
        }
      }
    } else {
      null
    },
  )
}

/**
 * Lays out the text along with the leading and trailing content, while always making sure that the text field does not
 * swallow the trailing content and make it disappear, while at the same time not going to 0.dp width if we used a
 * `weight(1f)` on it instead.
 */
@Composable
private fun RowTextFieldLayout(
  modifier: Modifier,
  centerContent: @Composable () -> Unit,
  leading: (@Composable () -> Unit)?,
  trailing: (@Composable () -> Unit)?,
  alignment: Alignment.Vertical = Alignment.CenterVertically,
) {
  Layout(
    modifier = modifier,
    contents = listOf(
      { centerContent() },
      { leading?.invoke() },
      { trailing?.invoke() },
    ),
  ) { measurables: List<List<Measurable>>, constraints: Constraints ->
    val centerMeasurable = measurables[0].first()
    val leadingMeasurable = measurables[1].firstOrNull()
    val trailingMeasurable = measurables[2].firstOrNull()

    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    val leadingPlaceable = leadingMeasurable?.measure(looseConstraints)
    val trailingPlaceable = trailingMeasurable?.measure(looseConstraints)
    val centerPlaceable = centerMeasurable.measure(
      looseConstraints.copy(
        maxWidth = if (looseConstraints.hasBoundedWidth) {
          (looseConstraints.maxWidth - (trailingPlaceable?.width ?: 0) - (leadingPlaceable?.width ?: 0))
            .coerceAtLeast(0)
        } else {
          looseConstraints.maxWidth
        },
      ),
    )

    val width = max(
      (leadingPlaceable?.width ?: 0) + (trailingPlaceable?.width ?: 0) + centerPlaceable.width,
      constraints.minWidth,
    )
    val height = maxOf(
      leadingPlaceable?.height ?: 0,
      trailingPlaceable?.height ?: 0,
      centerPlaceable.height,
    )
    layout(width, height) {
      leadingPlaceable?.place(0, alignment.align(leadingPlaceable.height, height))
      trailingPlaceable?.place(width - trailingPlaceable.width, alignment.align(trailingPlaceable.height, height))
      centerPlaceable.place(
        leadingPlaceable?.width ?: 0,
        alignment.align(centerPlaceable.height, height),
      )
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

private const val InnerTextFieldId = "InnerTextField"
private const val LabelId = "Label"
private const val SuffixId = "Suffix"
private const val LeadingContentId = "LeadingContent"
private const val TrailingContentId = "TrailingContent"
private const val ContainerId = "Container"

private const val TextFieldLabelAnimationDuration = 150
private val LabelTransitionAnimationSpec: FiniteAnimationSpec<Rect> = tween<Rect>(TextFieldLabelAnimationDuration)

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
        leadingContent = { Icon(HedvigIcons.Image, null) },
        trailingContent = { Icon(HedvigIcons.Image, null) },
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
