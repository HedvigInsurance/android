package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Default
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.RadioOptionState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionState.ChosenLocked
import com.hedvig.android.design.system.hedvig.RadioOptionState.NotChosen
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.LargeSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.SmallSizeRadioOptionTokens

@Composable
fun RadioOption(
  optionText: String,
  onClick: () -> Unit,
  state: RadioOptionState,
  modifier: Modifier = Modifier,
  radioOptionStyle: RadioOptionStyle = RadioOptionDefaults.radioOptionStyle,
  radioOptionSize: RadioOptionDefaults.RadioOptionSize = RadioOptionDefaults.radioOptionSize,
  interactionSource: MutableInteractionSource? = null,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  Surface(
    onClick = onClick,
    modifier = modifier.semantics { role = Role.RadioButton },
    enabled = when (state) {
      ChosenLocked -> false
      else -> true
    },
    shape = radioOptionSize.size(radioOptionStyle).shape,
    color = radioOptionColors.containerColor,
    interactionSource = interactionSource,
  ) {
    val optionTextColor = radioOptionColors.optionTextColor(state)
    val labelTextColor = radioOptionColors.labelTextColor(state)
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
          SelectIndicationCircle(state)
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
          SelectIndicationCircle(state)
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
          SelectIndicationCircle(state)
        }
        LeftAligned -> {
          SelectIndicationCircle(state)
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

sealed interface IconResource {
  data class Vector(val imageVector: ImageVector) : IconResource

  data class Painter(val painterResId: Int) : IconResource
}

@Composable
fun SelectIndicationCircle(state: RadioOptionState, modifier: Modifier = Modifier) {
  CheckItemAnimation(state) { currentState: RadioOptionState ->
    Box(
      contentAlignment = Alignment.Center,
    ) {
      Spacer(
        modifier
          .size(24.dp)
          .clip(CircleShape)
          .then(
            when (currentState) {
              Chosen -> Modifier.border(8.dp, radioOptionColors.chosenIndicatorColor, CircleShape)
              NotChosen -> Modifier.border(2.dp, radioOptionColors.notChosenIndicatorColor, CircleShape)
              ChosenLocked -> Modifier.border(8.dp, radioOptionColors.disabledIndicatorColor, CircleShape)
            },
          ),
      )
    }
  }
}

@Composable
private fun CheckItemAnimation(state: RadioOptionState, content: @Composable (state: RadioOptionState) -> Unit) {
  val selectedTransition = updateTransition(state)
  selectedTransition.AnimatedContent(
    transitionSpec = {
      fadeIn(tween(durationMillis = 200, easing = EaseInOut)) togetherWith fadeOut(tween(200, easing = EaseInOut))
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

    data class Icon(val iconResource: IconResource) : RadioOptionStyle // we need another solution here. Content?

    data object LeftAligned : RadioOptionStyle
  }

  enum class RadioOptionSize {
    Large,
    Medium,
    Small,
  }
}

enum class RadioOptionState {
  Chosen,
  NotChosen,
  ChosenLocked,
}

private fun RadioOptionDefaults.RadioOptionSize.size(style: RadioOptionStyle): RadioOptionSize {
  return when (this) {
    RadioOptionDefaults.RadioOptionSize.Large -> RadioOptionSize.Large(style)
    RadioOptionDefaults.RadioOptionSize.Medium -> RadioOptionSize.Medium(style)
    RadioOptionDefaults.RadioOptionSize.Small -> RadioOptionSize.Small(style)
  }
}

@Immutable
private data class RadioOptionColors(
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
  fun optionTextColor(state: RadioOptionState): Color = when (state) {
    ChosenLocked -> disabledOptionTextColor
    else -> optionTextColor
  }

  @Stable
  fun labelTextColor(state: RadioOptionState): Color = when (state) {
    ChosenLocked -> disabledLabelTextColor
    else -> labelTextColor
  }
}

private val radioOptionColors: RadioOptionColors
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

private sealed interface RadioOptionSize {
  val contentPadding: PaddingValues

  @get:Composable
  val optionTextStyle: TextStyle

  @get:Composable
  val labelTextStyle: TextStyle

  @get:Composable
  val shape: Shape

  data class Large(val style: RadioOptionStyle) : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeRadioOptionTokens.topPadding(style),
      bottom = LargeSizeRadioOptionTokens.bottomPadding(style),
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
      top = MediumSizeRadioOptionTokens.topPadding(style),
      bottom = MediumSizeRadioOptionTokens.bottomPadding(style),
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
      top = SmallSizeRadioOptionTokens.topPadding(style),
      bottom = SmallSizeRadioOptionTokens.bottomPadding(style),
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
        Modifier.width(330.dp).padding(16.dp),
      ) {
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = ChosenLocked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Large,
        )
        Spacer(Modifier.height(16.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = ChosenLocked,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
        )
        Spacer(Modifier.height(16.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = Chosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = NotChosen,
          radioOptionStyle = style,
          radioOptionSize = RadioOptionDefaults.RadioOptionSize.Small,
        )
        Spacer(Modifier.height(4.dp))
        RadioOption(
          optionText = "Option",
          onClick = {},
          state = ChosenLocked,
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
      //   RadioOptionStyle.Icon(IconResource.Vector(HedvigIcons.FlagSweden)),
      LeftAligned,
    ),
  )
