package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Amber
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Blue
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Frosted
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Green
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Pink
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Purple
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Red
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Teal
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Yellow
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.DARK
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.HighlightLabelTokens

@Composable
fun HighlightLabel(labelText: String, size: HighLightSize, color: HighlightColor, modifier: Modifier = Modifier) {
  val surfaceColor: Color = when (color) {
    is Amber -> {
      when (color.shade) {
        LIGHT -> highLightColors.amberLight
        MEDIUM -> highLightColors.amberMedium
        DARK -> highLightColors.amberDark
      }
    }

    is Blue -> {
      when (color.shade) {
        LIGHT -> highLightColors.blueLight
        MEDIUM -> highLightColors.blueMedium
        DARK -> highLightColors.blueDark
      }
    }

    is Green -> {
      when (color.shade) {
        LIGHT -> highLightColors.greenLight
        MEDIUM -> highLightColors.greenMedium
        DARK -> highLightColors.greenDark
      }
    }

    is Grey -> {
      when (color.translucent) {
        false -> {
          when (color.shade) {
            LIGHT -> highLightColors.greyLight
            MEDIUM -> highLightColors.greyMedium
            DARK -> highLightColors.greyDark
          }
        }

        true -> {
          when (color.shade) {
            LIGHT -> highLightColors.greyLightTranslucent
            MEDIUM -> highLightColors.greyMediumTranslucent
            DARK -> highLightColors.greyDarkTranslucent
          }
        }
      }
    }

    is Pink -> {
      when (color.shade) {
        LIGHT -> highLightColors.pinkLight
        MEDIUM -> highLightColors.pinkMedium
        DARK -> highLightColors.pinkDark
      }
    }

    is Purple -> {
      when (color.shade) {
        LIGHT -> highLightColors.purpleLight
        MEDIUM -> highLightColors.purpleMedium
        DARK -> highLightColors.purpleDark
      }
    }

    is Red -> {
      when (color.shade) {
        LIGHT -> highLightColors.redLight
        MEDIUM -> highLightColors.redMedium
        DARK -> highLightColors.redDark
      }
    }

    is Teal -> {
      when (color.shade) {
        LIGHT -> highLightColors.tealLight
        MEDIUM -> highLightColors.tealMedium
        DARK -> highLightColors.tealDark
      }
    }

    is Yellow -> {
      when (color.shade) {
        LIGHT -> highLightColors.yellowLight
        MEDIUM -> highLightColors.yellowMedium
        DARK -> highLightColors.yellowDark
      }
    }

    is Frosted -> when (color.shade) {
      LIGHT -> highLightColors.frostedLight
      MEDIUM -> highLightColors.frostedMedium
      DARK -> highLightColors.frostedDark
    }
  }
  val textColor = when (color) {
    is Grey -> {
      when (color.shade) {
        LIGHT -> highLightColors.textColorForGreyLight
        MEDIUM -> highLightColors.textColorForGreyMedium
        DARK -> highLightColors.textColorForGreyDark
      }
    }

    is Frosted -> {
      highLightColors.textColorForFrosted
    }

    else -> highLightColors.defaultTextColor
  }
  Surface(
    modifier = modifier,
    shape = size.shape,
    color = surfaceColor,
  ) {
    HedvigText(
      text = labelText,
      textAlign = TextAlign.Center,
      style = size.textStyle,
      color = textColor,
      modifier = Modifier.padding(size.contentPadding),
    )
  }
}

object HighlightLabelDefaults {
  sealed class HighLightSize {
    abstract val contentPadding: PaddingValues

    @get:Composable
    abstract val shape: Shape

    @get:Composable
    abstract val textStyle: TextStyle

    data object Large : HighLightSize() {
      override val contentPadding: PaddingValues = PaddingValues(
        top = HighlightLabelTokens.LargePaddingTop,
        bottom = HighlightLabelTokens.LargePaddingBottom,
        start = HighlightLabelTokens.LargePaddingStart,
        end = HighlightLabelTokens.LargePaddingEnd,
      )
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.ContainerShapeLarge.value

      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.TypographyLarge.value
    }

    data object Medium : HighLightSize() {
      override val contentPadding: PaddingValues = PaddingValues(
        top = HighlightLabelTokens.MediumPaddingTop,
        bottom = HighlightLabelTokens.MediumPaddingBottom,
        start = HighlightLabelTokens.MediumPaddingStart,
        end = HighlightLabelTokens.MediumPaddingEnd,
      )
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.ContainerShapeMedium.value
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.TypographyMedium.value
    }

    data object Small : HighLightSize() {
      override val contentPadding: PaddingValues = PaddingValues(
        top = HighlightLabelTokens.SmallPaddingTop,
        bottom = HighlightLabelTokens.SmallPaddingBottom,
        start = HighlightLabelTokens.SmallPaddingStart,
        end = HighlightLabelTokens.SmallPaddingEnd,
      )
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.ContainerShapeSmall.value
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = HighlightLabelTokens.TypographySmall.value
    }
  }

  enum class HighlightShade {
    LIGHT,
    MEDIUM,
    DARK,
  }

  sealed class HighlightColor {
    abstract val shade: HighlightShade

    data class Blue(override val shade: HighlightShade) : HighlightColor()

    data class Teal(override val shade: HighlightShade) : HighlightColor()

    data class Purple(override val shade: HighlightShade) : HighlightColor()

    data class Green(override val shade: HighlightShade) : HighlightColor()

    data class Yellow(override val shade: HighlightShade) : HighlightColor()

    data class Amber(override val shade: HighlightShade) : HighlightColor()

    data class Red(override val shade: HighlightShade) : HighlightColor()

    data class Pink(override val shade: HighlightShade) : HighlightColor()

    // Temp translucent grey colors https://hedviginsurance.slack.com/archives/C03U9C6Q7TP/p1727340030201129
    data class Grey(override val shade: HighlightShade, val translucent: Boolean = false) : HighlightColor()

    data class Frosted(override val shade: HighlightShade) : HighlightColor()

    companion object {
      // todo DS add new outline "color"
      val Outline = Grey(HighlightShade.LIGHT)
    }
  }
}

private data class HighLightColors(
  val blueLight: Color,
  val blueMedium: Color,
  val blueDark: Color,
  val tealLight: Color,
  val tealMedium: Color,
  val tealDark: Color,
  val purpleLight: Color,
  val purpleMedium: Color,
  val purpleDark: Color,
  val greenLight: Color,
  val greenMedium: Color,
  val greenDark: Color,
  val yellowLight: Color,
  val yellowMedium: Color,
  val yellowDark: Color,
  val amberLight: Color,
  val amberMedium: Color,
  val amberDark: Color,
  val redLight: Color,
  val redMedium: Color,
  val redDark: Color,
  val pinkLight: Color,
  val pinkMedium: Color,
  val pinkDark: Color,
  val greyLight: Color,
  val greyMedium: Color,
  val greyDark: Color,
  val greyLightTranslucent: Color,
  val greyMediumTranslucent: Color,
  val greyDarkTranslucent: Color,
  val defaultTextColor: Color,
  val textColorForGreyLight: Color,
  val textColorForGreyMedium: Color,
  val textColorForGreyDark: Color,
  val textColorForFrosted: Color,
  val frostedLight: Color,
  val frostedMedium: Color,
  val frostedDark: Color,
)

private val highLightColors: HighLightColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      HighLightColors(
        blueLight = fromToken(ColorSchemeKeyTokens.HighlightBlueFill1),
        blueMedium = fromToken(ColorSchemeKeyTokens.HighlightBlueFill2),
        blueDark = fromToken(ColorSchemeKeyTokens.HighlightBlueFill3),
        tealLight = fromToken(ColorSchemeKeyTokens.HighlightTealFill1),
        tealMedium = fromToken(ColorSchemeKeyTokens.HighlightTealFill2),
        tealDark = fromToken(ColorSchemeKeyTokens.HighlightTealFill3),
        purpleLight = fromToken(ColorSchemeKeyTokens.HighlightPurpleFill1),
        purpleMedium = fromToken(ColorSchemeKeyTokens.HighlightPurpleFill2),
        purpleDark = fromToken(ColorSchemeKeyTokens.HighlightPurpleFill3),
        greenLight = fromToken(ColorSchemeKeyTokens.HighlightGreenFill1),
        greenMedium = fromToken(ColorSchemeKeyTokens.HighlightGreenFill2),
        greenDark = fromToken(ColorSchemeKeyTokens.HighlightGreenFill3),
        yellowLight = fromToken(ColorSchemeKeyTokens.HighlightYellowFill1),
        yellowMedium = fromToken(ColorSchemeKeyTokens.HighlightYellowFill2),
        yellowDark = fromToken(ColorSchemeKeyTokens.HighlightYellowFill3),
        amberLight = fromToken(ColorSchemeKeyTokens.HighlightAmberFill1),
        amberMedium = fromToken(ColorSchemeKeyTokens.HighlightAmberFill2),
        amberDark = fromToken(ColorSchemeKeyTokens.HighlightAmberFill3),
        redLight = fromToken(ColorSchemeKeyTokens.HighlightRedFill1),
        redMedium = fromToken(ColorSchemeKeyTokens.HighlightRedFill2),
        redDark = fromToken(ColorSchemeKeyTokens.HighlightRedFill3),
        pinkLight = fromToken(ColorSchemeKeyTokens.HighlightPinkFill1),
        pinkMedium = fromToken(ColorSchemeKeyTokens.HighlightPinkFill1),
        pinkDark = fromToken(ColorSchemeKeyTokens.HighlightPinkFill1),
        greyLight = fromToken(ColorSchemeKeyTokens.SurfacePrimary),
        greyMedium = fromToken(ColorSchemeKeyTokens.SurfaceSecondary),
        greyDark = fromToken(ColorSchemeKeyTokens.BackgroundNegative),
        greyLightTranslucent = fromToken(ColorSchemeKeyTokens.SurfacePrimaryTransparent),
        greyMediumTranslucent = fromToken(ColorSchemeKeyTokens.SurfaceSecondaryTransparent),
        greyDarkTranslucent = fromToken(ColorSchemeKeyTokens.BackgroundNegative),
        defaultTextColor = fromToken(ColorSchemeKeyTokens.TextBlack),
        textColorForGreyLight = fromToken(ColorSchemeKeyTokens.TextPrimary),
        textColorForGreyMedium = fromToken(ColorSchemeKeyTokens.TextPrimary),
        textColorForGreyDark = fromToken(ColorSchemeKeyTokens.TextNegative),
        textColorForFrosted = fromToken(ColorSchemeKeyTokens.TextWhite),
        frostedLight = fromToken(ColorSchemeKeyTokens.SurfaceSecondaryTransparent),
        frostedMedium = fromToken(ColorSchemeKeyTokens.FillTertiaryTransparent),
        frostedDark = fromToken(ColorSchemeKeyTokens.FillSecondaryTransparent),
      )
    }
  }
