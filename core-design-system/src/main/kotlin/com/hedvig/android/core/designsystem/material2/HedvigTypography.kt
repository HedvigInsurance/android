package com.hedvig.android.core.designsystem.material2

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.theme.SansStandard

internal val HedvigTypography: Typography
  @Composable
  get() = Typography(
    defaultFontFamily = SansStandard,
    h3 = MaterialTheme.typography.h3.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 48.sp,
      lineHeight = 58.sp,
      letterSpacing = (-1f).percentage.em,
    ),
    h4 = MaterialTheme.typography.h4.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = (-0.25f).percentage.em,
    ),
    h5 = MaterialTheme.typography.h5.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
      letterSpacing = (-0.25f).percentage.em,
    ),
    h6 = MaterialTheme.typography.h6.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0f.em,
    ),
    subtitle1 = MaterialTheme.typography.subtitle1.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0f.em,
    ),
    subtitle2 = MaterialTheme.typography.subtitle2.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0f.em,
    ),
    body1 = MaterialTheme.typography.body1.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0f.em,
    ),
    body2 = MaterialTheme.typography.body2.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0f.em,
    ),
    button = MaterialTheme.typography.button.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = (-0.1f).percentage.em,
    ),
    caption = MaterialTheme.typography.caption.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.em,
    ),
    overline = MaterialTheme.typography.overline.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 10.sp,
      lineHeight = 16.sp,
      letterSpacing = 1f.percentage.em,
    ),
  )

/**
 * Text spacing in figma is specified in a percentage value. To represent that, we need to use "em" which is
 * "relative font size", and use this `percentage` function to use the exact same number as in Figma.
 */
private val Float.percentage: Float
  get() = this / 100

@Preview
@Composable
private fun TypographyPreview() {
  Surface {
    Column {
      Text("Headline 3", style = HedvigTypography.h3)
      Text("Headline 4", style = HedvigTypography.h4)
      Text("Headline 5", style = HedvigTypography.h5)
      Text("Headline 6", style = HedvigTypography.h6)
      Text("Subtitle 1", style = HedvigTypography.subtitle1)
      Text("Subtitle 2", style = HedvigTypography.subtitle2)
      Text("Body 1", style = HedvigTypography.body1)
      Text("Body 2", style = HedvigTypography.body2)
      Text("Button", style = HedvigTypography.button)
      Text("Caption", style = HedvigTypography.caption)
      Text("Overline", style = HedvigTypography.overline)
    }
  }
}
