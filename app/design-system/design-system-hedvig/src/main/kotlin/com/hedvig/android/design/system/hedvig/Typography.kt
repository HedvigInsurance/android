package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyTokens

@Immutable
data class Typography(
  val bodyLarge: TextStyle = TypographyTokens.BodyLarge,
  val bodyMedium: TextStyle = TypographyTokens.BodyMedium,
  val bodySmall: TextStyle = TypographyTokens.BodySmall,
  val displayLarge: TextStyle = TypographyTokens.DisplayLarge,
  val displayMedium: TextStyle = TypographyTokens.DisplayMedium,
  val displaySmall: TextStyle = TypographyTokens.DisplaySmall,
  val headlineLarge: TextStyle = TypographyTokens.HeadlineLarge,
  val headlineMedium: TextStyle = TypographyTokens.HeadlineMedium,
  val headlineSmall: TextStyle = TypographyTokens.HeadlineSmall,
  val label: TextStyle = TypographyTokens.Label,
  val finePrint: TextStyle = TypographyTokens.FinePrint,
)

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

internal val LocalTypography = staticCompositionLocalOf { Typography() }
