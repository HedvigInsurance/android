package com.hedvig.android.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/**
 * Used in specific places instead of setting it to a particular font size.
 * Particularly, now used in the Home screen as the big header.
 */
val SerifBookSmall = FontFamily(
  Font(hedvig.resources.R.font.hedvig_letters_small),
)

internal val SansStandard = FontFamily(
  Font(hedvig.resources.R.font.hedvig_letters_standard),
)
