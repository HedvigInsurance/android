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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Default
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.LargeSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.SmallSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.TweenAnimationTokens

// used in RadioOption and Checkbox composables
data class RadioOptionData(
  val id: String,
  val optionText: String,
  val chosenState: ChosenState,
  val lockedState: LockedState = NotLocked,
)

sealed interface RadioOptionGroupData {
  val radioOptionData: RadioOptionData

  data class RadioOptionGroupDataSimple(
    override val radioOptionData: RadioOptionData,
  ) :
    RadioOptionGroupData

  data class RadioOptionGroupDataWithLabel(
    override val radioOptionData: RadioOptionData,
    val labelText: String,
  ) : RadioOptionGroupData

  data class RadioOptionGroupDataWithIcon(
    override val radioOptionData: RadioOptionData,
    val iconResource: IconResource,
  ) : RadioOptionGroupData
}

@Composable
internal fun RadioOption(
  data: RadioOptionData,
  radioOptionStyle: RadioOptionStyle,
  radioOptionSize: RadioOptionDefaults.RadioOptionSize,
  groupLockedState: LockedState,
  modifier: Modifier = Modifier,
  onOptionClick: (() -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  RadioOption(
    optionText = data.optionText,
    onClick = onOptionClick,
    chosenState = data.chosenState,
    modifier = modifier,
    lockedState = calculateLockedStateForItemInGroup(data, groupLockedState),
    radioOptionStyle = radioOptionStyle,
    radioOptionSize = radioOptionSize,
    interactionSource = interactionSource,
  )
}

internal fun calculateLockedStateForItemInGroup(data: RadioOptionData, groupLockedState: LockedState): LockedState {
  return if (groupLockedState == Locked) Locked else data.lockedState
}

@Composable
fun RadioOption(
  optionText: String,
  chosenState: ChosenState,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  lockedState: LockedState = NotLocked,
  radioOptionStyle: RadioOptionStyle = RadioOptionDefaults.radioOptionStyle,
  radioOptionSize: RadioOptionDefaults.RadioOptionSize = RadioOptionDefaults.radioOptionSize,
  interactionSource: MutableInteractionSource? = null,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val clickableModifier = if (onClick != null) {
    modifier
      .clip(radioOptionSize.size(radioOptionStyle).shape)
      .semantics { role = Role.RadioButton }
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
  } else {
    modifier.semantics { role = Role.RadioButton }
  }
  Surface(
    modifier = clickableModifier,
    shape = radioOptionSize.size(radioOptionStyle).shape,
    color = radioOptionColors.containerColor,
  ) {
    val optionTextColor = radioOptionColors.optionTextColor(lockedState)
    val labelTextColor = radioOptionColors.labelTextColor(lockedState)
    Row(
      modifier = Modifier.padding(radioOptionSize.size(radioOptionStyle).contentPadding),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      when (radioOptionStyle) {
        Default -> {
          HedvigText(
            optionText,
            style = radioOptionSize.size(radioOptionStyle).optionTextStyle,
            color = optionTextColor,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(8.dp))
          SelectIndicationCircle(chosenState, lockedState)
        }

        is RadioOptionStyle.Icon -> {
          when (radioOptionStyle.iconResource) {
            is IconResource.Vector -> {
              Icon(
                imageVector = radioOptionStyle.iconResource.imageVector,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                  .size(32.dp),
              )
            }

            is IconResource.Painter -> {
              Image(
                painter = painterResource(id = radioOptionStyle.iconResource.painterResId),
                contentDescription = null,
                modifier = Modifier
                  .size(32.dp),
              )
            }
          }
          Spacer(Modifier.width(8.dp))
          HedvigText(
            optionText,
            style = radioOptionSize.size(radioOptionStyle).optionTextStyle,
            modifier = Modifier.weight(1f),
            color = optionTextColor,
          )
          Spacer(Modifier.width(8.dp))
          SelectIndicationCircle(chosenState, lockedState)
        }

        is Label -> {
          Column(Modifier.weight(1f)) {
            HedvigText(
              optionText,
              style = radioOptionSize.size(radioOptionStyle).optionTextStyle,
              color = optionTextColor,
            )
            HedvigText(
              radioOptionStyle.labelText,
              style = radioOptionSize.size(radioOptionStyle).labelTextStyle,
              color = labelTextColor,
            )
          }
          Spacer(Modifier.width(8.dp))
          SelectIndicationCircle(chosenState, lockedState)
        }

        LeftAligned -> {
          SelectIndicationCircle(chosenState, lockedState)
          Spacer(Modifier.width(8.dp))
          HedvigText(
            optionText,
            style = radioOptionSize.size(radioOptionStyle).optionTextStyle,
            color = optionTextColor,
            modifier = Modifier.weight(1f),
          )
        }
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
                  Locked -> Modifier.border(8.dp, radioOptionColors.disabledIndicatorColor, CircleShape)
                  NotLocked -> Modifier.border(8.dp, radioOptionColors.chosenIndicatorColor, CircleShape)
                }
              }
              NotChosen -> {
                when (lockedState) {
                  Locked -> Modifier.border(8.dp, radioOptionColors.disabledIndicatorColor, CircleShape)
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

internal sealed interface RadioOptionSize {
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

@Preview
@Composable
private fun PreviewRadioOptionStyles(
  @PreviewParameter(RadioOptionStyleProvider::class) style: RadioOptionStyle,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(
        Modifier
          .width(330.dp)
          .padding(16.dp),
      ) {
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(16.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(16.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = Chosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          chosenState = NotChosen,
          lockedState = Locked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

private class RadioOptionStyleProvider :
  CollectionPreviewParameterProvider<RadioOptionStyle>(
    listOf(
      Default,
      Label("Label"),
      RadioOptionStyle.Icon(IconResource.Painter(hedvig.resources.R.drawable.pillow_hedvig)),
      RadioOptionStyle.Icon(IconResource.Vector(HedvigIcons.FlagSweden)),
      LeftAligned,
    ),
  )
