package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Default
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.LargeSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.SmallSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.TweenAnimationTokens
import hedvig.resources.R

@Composable
fun RadioOption(
  chosenState: ChosenState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  lockedState: LockedState = NotLocked,
  size: RadioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium.size(LeftAligned),
  optionContent: @Composable (radioButtonIcon: @Composable () -> Unit) -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val optionStateDesc = when (chosenState) {
    Chosen -> stringResource(R.string.TALKBACK_OPTION_SELECTED)
    NotChosen -> stringResource(R.string.TALKBACK_OPTION_NOT_SELECTED)
  }
  val clickableModifier =
    modifier
      .semantics(true) {
        role = Role.RadioButton
        stateDescription = optionStateDesc
      }
      .clip(size.shape)
      .clickable(
        enabled = when (lockedState) {
          Locked -> false
          NotLocked -> true
        },
        interactionSource = interactionSource,
        indication = LocalIndication.current,
      ) {
        onClick()
      }

  Surface(
    modifier = clickableModifier,
    shape = size.shape,
    color = radioOptionColors.containerColor,
  ) {
    Box(
      modifier = Modifier.padding(size.contentPadding),
      propagateMinConstraints = true,
    ) {
      optionContent {
        SelectIndicationCircle(chosenState, lockedState)
      }
    }
  }
}

@Composable
internal fun SelectIndicationCircle(
  chosenState: ChosenState,
  lockedState: LockedState,
  modifier: Modifier = Modifier,
) {
  CheckItemAnimation(chosenState) { currentState: ChosenState ->
    Box(
      contentAlignment = Alignment.Center,
    ) {
      Spacer(
        modifier
          .size(24.dp)
          .clip(CircleShape)
          .then(
            when (currentState) {
              Chosen -> {
                when (lockedState) {
                  Locked -> Modifier.border(8.dp, radioOptionColors.notChosenIndicatorColor, CircleShape)
                  NotLocked -> Modifier.border(8.dp, radioOptionColors.chosenIndicatorColor, CircleShape)
                }
              }

              NotChosen -> {
                when (lockedState) {
                  Locked -> Modifier.border(2.dp, radioOptionColors.notChosenIndicatorColor, CircleShape)
                  NotLocked -> Modifier.border(2.dp, radioOptionColors.notChosenIndicatorColor, CircleShape)
                }
              }
            },
          ),
      )
    }
  }
}

@Composable
internal fun CheckItemAnimation(state: ChosenState, content: @Composable (state: ChosenState) -> Unit) {
  val selectedTransition = updateTransition(state)
  selectedTransition.AnimatedContent(
    transitionSpec = {
      fadeIn(
        tween(
          durationMillis = TweenAnimationTokens.FastAnimationTokens.durationMillis,
          easing = TweenAnimationTokens.FastAnimationTokens.easing,
        ),
      ) togetherWith fadeOut(
        tween(
          durationMillis = TweenAnimationTokens.FastAnimationTokens.durationMillis,
          easing = TweenAnimationTokens.FastAnimationTokens.easing,
        ),
      )
    },
    contentAlignment = Alignment.Center,
  ) { currentState ->
    content(currentState)
  }
}

object RadioOptionDefaults {
  internal val radioOptionStyle: RadioOptionStyle = Default
  internal val radioOptionSize: RadioOptionSize = RadioOptionSize.Large

  sealed interface RadioOptionStyle {
    data object Default : RadioOptionStyle

    data class Label(val labelText: String) : RadioOptionStyle

    data class Icon(val iconResource: IconResource) : RadioOptionStyle

    data object LeftAligned : RadioOptionStyle
  }

  enum class RadioOptionSize {
    Large,
    Medium,
    Small,
  }
}

enum class ChosenState {
  Chosen,
  NotChosen,
}

enum class LockedState {
  Locked,
  NotLocked,
}

internal fun RadioOptionDefaults.RadioOptionSize.size(style: RadioOptionStyle): RadioOptionSize {
  return when (this) {
    RadioOptionDefaults.RadioOptionSize.Large -> RadioOptionSize.Large(style)
    RadioOptionDefaults.RadioOptionSize.Medium -> RadioOptionSize.Medium(style)
    RadioOptionDefaults.RadioOptionSize.Small -> RadioOptionSize.Small(style)
  }
}

@Immutable
internal data class RadioOptionColors(
  val containerColor: Color,
  val optionTextColor: Color,
  val labelTextColor: Color,
  val disabledOptionTextColor: Color,
  val disabledLabelTextColor: Color,
  val chosenIndicatorColor: Color,
  val notChosenIndicatorColor: Color,
  val disabledIndicatorColor: Color,
) {
  @Stable
  fun optionTextColor(state: LockedState): Color = when (state) {
    Locked -> disabledOptionTextColor
    else -> optionTextColor
  }

  @Stable
  fun labelTextColor(state: LockedState): Color = when (state) {
    Locked -> disabledLabelTextColor
    else -> labelTextColor
  }
}

internal val radioOptionColors: RadioOptionColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      RadioOptionColors(
        containerColor = fromToken(RadioOptionColorTokens.ContainerColor),
        optionTextColor = fromToken(RadioOptionColorTokens.OptionTextColor),
        labelTextColor = fromToken(RadioOptionColorTokens.LabelTextColor),
        disabledOptionTextColor = fromToken(RadioOptionColorTokens.DisabledOptionTextColor),
        disabledLabelTextColor = fromToken(RadioOptionColorTokens.DisabledLabelTextColor),
        chosenIndicatorColor = fromToken(RadioOptionColorTokens.ChosenIndicatorColor),
        notChosenIndicatorColor = fromToken(RadioOptionColorTokens.NotChosenIndicatorColor),
        disabledIndicatorColor = fromToken(RadioOptionColorTokens.DisabledIndicatorColor),
      )
    }
  }

sealed interface RadioOptionSize {
  val contentPadding: PaddingValues

  @get:Composable
  val optionTextStyle: TextStyle

  @get:Composable
  val labelTextStyle: TextStyle

  @get:Composable
  val shape: Shape

  data class Large(val style: RadioOptionStyle) : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeRadioOptionTokens.verticalPadding(style).calculateTopPadding(),
      bottom = LargeSizeRadioOptionTokens.verticalPadding(style).calculateBottomPadding(),
      start = LargeSizeRadioOptionTokens.HorizontalPadding,
      end = LargeSizeRadioOptionTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeRadioOptionTokens.LabelTextFont.value

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeRadioOptionTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeRadioOptionTokens.ContainerShape.value
  }

  data class Medium(val style: RadioOptionStyle) : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeRadioOptionTokens.verticalPadding(style).calculateTopPadding(),
      bottom = MediumSizeRadioOptionTokens.verticalPadding(style).calculateBottomPadding(),
      start = MediumSizeRadioOptionTokens.HorizontalPadding,
      end = MediumSizeRadioOptionTokens.HorizontalPadding,
    )

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeRadioOptionTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeRadioOptionTokens.ContainerShape.value

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeRadioOptionTokens.LabelTextFont.value
  }

  data class Small(val style: RadioOptionStyle) : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeRadioOptionTokens.verticalPadding(style).calculateTopPadding(),
      bottom = SmallSizeRadioOptionTokens.verticalPadding(style).calculateBottomPadding(),
      start = SmallSizeRadioOptionTokens.HorizontalPadding,
      end = SmallSizeRadioOptionTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeRadioOptionTokens.LabelTextFont.value

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeRadioOptionTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeRadioOptionTokens.ContainerShape.value
  }
}
