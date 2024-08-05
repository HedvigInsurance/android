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
import androidx.compose.ui.unit.Dp
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Default
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Detailed
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDetailedToggleTokens
import com.hedvig.android.design.system.hedvig.tokens.ToggleColorTokens

@Composable
fun HedvigToggle() {
}

object ToggleDefaults {
  internal val toggleStyle: ToggleStyle = Default(ToggleDefaultStyleSize.Large)

  sealed class ToggleStyle {
    class Default(size: ToggleDefaultStyleSize) : ToggleStyle()

    class Detailed(size: ToggleDetailedStyleSize) : ToggleStyle()
  }

  enum class ToggleDefaultStyleSize {
    Large,
    Medium,
    Small,
  }

  enum class ToggleDetailedStyleSize {
    Large,
    Small,
  }
}

private val ToggleDefaults.ToggleStyle.style: ToggleStyle
  get() = when (this) {
    is Default -> ToggleStyle.Default
    is Detailed -> ToggleStyle.Detailed
  }

private val ToggleDefaults.ToggleDefaultStyleSize.size: ToggleDefaultStyleSize
  get() = when (this) {
    ToggleDefaults.ToggleDefaultStyleSize.Large -> ToggleDefaultStyleSize.Large
    ToggleDefaults.ToggleDefaultStyleSize.Medium -> ToggleDefaultStyleSize.Medium
    ToggleDefaults.ToggleDefaultStyleSize.Small -> ToggleDefaultStyleSize.Small
  }

private val ToggleDefaults.ToggleDetailedStyleSize.size: ToggleDetailedStyleSize
  get() = when (this) {
    ToggleDefaults.ToggleDetailedStyleSize.Large -> ToggleDetailedStyleSize.Large
    ToggleDefaults.ToggleDetailedStyleSize.Small -> ToggleDetailedStyleSize.Small
  }

@Immutable
private data class ToggleColors(
  val containerColor: Color,
  val labelColor: Color,
  val descriptionColor: Color,
  val pulsatingContainerColor: Color,
  val pulsatingLabelColor: Color,
  val pulsatingDescriptionColor: Color,
  val toggleTopColor: Color,
  val toggleBackgroundOnColor: Color,
  val toggleBackgroundOffColor: Color,
) {
  @Stable
  internal fun toggleBackgroundColor(enabled: Boolean): Color = when {
    enabled -> toggleBackgroundOnColor
    else -> toggleBackgroundOffColor
  }

  @Stable
  internal fun containerColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingContainerColor
    else -> containerColor
  }

  @Stable
  internal fun labelColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingLabelColor
    else -> labelColor
  }

  @Stable
  internal fun descriptionColor(pulsating: Boolean): Color = when {
    pulsating -> pulsatingDescriptionColor
    else -> descriptionColor
  }
}

private sealed interface ToggleDefaultStyleSize {
  val contentPadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

  @get:Composable
  val shape: Shape

  object Large : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeDefaultToggleTokens.TopPadding,
      bottom = LargeSizeDefaultToggleTokens.BottomPadding,
      start = LargeSizeDefaultToggleTokens.HorizontalPadding,
      end = LargeSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDefaultToggleTokens.ContainerShape.value
  }

  object Medium : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeDefaultToggleTokens.TopPadding,
      bottom = MediumSizeDefaultToggleTokens.BottomPadding,
      start = MediumSizeDefaultToggleTokens.HorizontalPadding,
      end = MediumSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeDefaultToggleTokens.ContainerShape.value
  }

  object Small : ToggleDefaultStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeDefaultToggleTokens.TopPadding,
      bottom = SmallSizeDefaultToggleTokens.BottomPadding,
      start = SmallSizeDefaultToggleTokens.HorizontalPadding,
      end = SmallSizeDefaultToggleTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDefaultToggleTokens.ContainerShape.value
  }
}

private sealed interface ToggleDetailedStyleSize {
  val contentPadding: PaddingValues

  val spacerHeight: Dp

  @get:Composable
  val labelTextStyle: TextStyle

  @get:Composable
  val descriptionTextStyle: TextStyle

  @get:Composable
  val shape: Shape

  object Large : ToggleDetailedStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeDetailedToggleTokens.TopPadding,
      bottom = LargeSizeDetailedToggleTokens.BottomPadding,
      start = LargeSizeDetailedToggleTokens.HorizontalPadding,
      end = LargeSizeDetailedToggleTokens.HorizontalPadding,
    )

    override val spacerHeight: Dp = LargeSizeDetailedToggleTokens.SpacerHeight

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.LabelTextFont.value

    override val descriptionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeDetailedToggleTokens.ContainerShape.value
  }

  object Small : ToggleDetailedStyleSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeDetailedToggleTokens.TopPadding,
      bottom = SmallSizeDetailedToggleTokens.BottomPadding,
      start = SmallSizeDetailedToggleTokens.HorizontalPadding,
      end = SmallSizeDetailedToggleTokens.HorizontalPadding,
    )

    override val spacerHeight: Dp = SmallSizeDetailedToggleTokens.SpacerHeight

    override val descriptionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.LabelTextFont.value

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeDetailedToggleTokens.ContainerShape.value
  }
}

private sealed interface ToggleStyle {
  val toggleColors: ToggleColors
    @Composable
    get() = with(HedvigTheme.colorScheme) {
      remember(this) {
        ToggleColors(
          containerColor = fromToken(ToggleColorTokens.ContainerColor),
          labelColor = fromToken(ToggleColorTokens.ContainerColor),
          descriptionColor = fromToken(ToggleColorTokens.ContainerColor),
          pulsatingContainerColor = fromToken(ToggleColorTokens.ContainerColor),
          pulsatingLabelColor = fromToken(ToggleColorTokens.ContainerColor),
          pulsatingDescriptionColor = fromToken(ToggleColorTokens.ContainerColor),
          toggleTopColor = fromToken(ToggleColorTokens.ContainerColor),
          toggleBackgroundOnColor = fromToken(ToggleColorTokens.ContainerColor),
          toggleBackgroundOffColor = fromToken(ToggleColorTokens.ContainerColor),
        )
      }
    }

  data object Default : ToggleStyle

  data object Detailed : ToggleStyle
}
