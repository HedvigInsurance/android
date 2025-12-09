package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.hedvig.android.design.system.hedvig.tokens.TypefaceTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyTokens
import com.hedvig.android.design.system.hedvig.tokens.toFontFamily
import com.hedvig.android.design.system.hedvig.tokens.toTextStyle

@Immutable
data class Typography(
  val bodyLarge: TextStyle,
  val bodyMedium: TextStyle,
  val bodySmall: TextStyle, // The default typography of the app
  val displayLarge: TextStyle,
  val displayMedium: TextStyle,
  val displaySmall: TextStyle,
  val headlineLarge: TextStyle,
  val headlineMedium: TextStyle,
  val headlineSmall: TextStyle,
  val label: TextStyle,
  val finePrint: TextStyle,
  val serif: FontFamily,
  val sans: FontFamily,
)

internal val HedvigTypography: Typography
  @Composable
  get() = Typography(
    bodyLarge = TypographyTokens.BodyLarge.toTextStyle(),
    bodyMedium = TypographyTokens.BodyMedium.toTextStyle(),
    bodySmall = TypographyTokens.BodySmall.toTextStyle(), // The default typography of the app
    displayLarge = TypographyTokens.DisplayLarge.toTextStyle(),
    displayMedium = TypographyTokens.DisplayMedium.toTextStyle(),
    displaySmall = TypographyTokens.DisplaySmall.toTextStyle(),
    headlineLarge = TypographyTokens.HeadlineLarge.toTextStyle(),
    headlineMedium = TypographyTokens.HeadlineMedium.toTextStyle(),
    headlineSmall = TypographyTokens.HeadlineSmall.toTextStyle(),
    label = TypographyTokens.Label.toTextStyle(),
    finePrint = TypographyTokens.FinePrint.toTextStyle(),
    serif = TypefaceTokens.Serif.toFontFamily(),
    sans = TypefaceTokens.Sans.toFontFamily(),
  )

@ReadOnlyComposable
@Composable
internal fun Typography.fromToken(value: TypographyKeyTokens): TextStyle {
  return when (value) {
    TypographyKeyTokens.BodyLarge -> bodyLarge
    TypographyKeyTokens.BodyMedium -> bodyMedium
    TypographyKeyTokens.BodySmall -> bodySmall
    TypographyKeyTokens.DisplayLarge -> displayLarge
    TypographyKeyTokens.DisplayMedium -> displayMedium
    TypographyKeyTokens.DisplaySmall -> displaySmall
    TypographyKeyTokens.HeadlineLarge -> headlineLarge
    TypographyKeyTokens.HeadlineMedium -> headlineMedium
    TypographyKeyTokens.HeadlineSmall -> headlineSmall
    TypographyKeyTokens.Label -> label
    TypographyKeyTokens.FinePrint -> finePrint
  }
}

internal val TypographyKeyTokens.value: TextStyle
  @Composable
  @ReadOnlyComposable
  get() = HedvigTheme.typography.fromToken(this)

internal val LocalTypography = staticCompositionLocalOf<Typography> {
  error("LocalTypography not provided")
}
