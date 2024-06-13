package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionState.ChosenLocked
import com.hedvig.android.design.system.hedvig.RadioOptionState.NotChosen
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.LargeSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.SmallSizeRadioOptionTokens

object RadioOptionDefaults {
  internal val radioOptionStyle: RadioOptionStyle = RadioOptionStyle.Default
  internal val radioOptionSize: RadioOptionSize = RadioOptionSize.Large

  enum class RadioOptionStyle {
    Default,
    Label,
    Icon,
    LeftAligned,
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
  internal fun containerColor(): Color = containerColor

  @Stable
  internal fun optionTextColor(state: RadioOptionState): Color = when (state) {
    ChosenLocked -> disabledOptionTextColor
    else -> optionTextColor
  }

  @Stable
  internal fun labelTextColor(state: RadioOptionState): Color = when (state) {
    ChosenLocked -> disabledLabelTextColor
    else -> labelTextColor
  }

  @Stable
  internal fun indicatorColor(state: RadioOptionState): Color = when (state) {
    Chosen -> chosenIndicatorColor
    NotChosen -> notChosenIndicatorColor
    ChosenLocked -> disabledIndicatorColor
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
