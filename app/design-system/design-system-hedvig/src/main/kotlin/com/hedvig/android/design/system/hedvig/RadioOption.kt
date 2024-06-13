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
import com.hedvig.android.design.system.hedvig.RadioOptionState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionState.ChosenLocked
import com.hedvig.android.design.system.hedvig.RadioOptionState.NotChosen
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeButtonTokens

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

  data object Large : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeButtonTokens.TopPadding,
      bottom = LargeSizeButtonTokens.BottomPadding,
      start = LargeSizeButtonTokens.HorizontalPadding,
      end = LargeSizeButtonTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.LabelTextFont.value

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.LabelTextFont.value // todo

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.ContainerShape.value
  }

  data object Medium : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeButtonTokens.TopPadding,
      bottom = MediumSizeButtonTokens.BottomPadding,
      start = MediumSizeButtonTokens.HorizontalPadding,
      end = MediumSizeButtonTokens.HorizontalPadding,
    )

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeButtonTokens.ContainerShape.value

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.LabelTextFont.value // todo
  }

  data object Small : RadioOptionSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeButtonTokens.TopPadding,
      bottom = SmallSizeButtonTokens.BottomPadding,
      start = SmallSizeButtonTokens.HorizontalPadding,
      end = SmallSizeButtonTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.LabelTextFont.value // todo

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeButtonTokens.ContainerShape.value
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
