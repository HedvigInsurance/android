package com.hedvig.android.core.designsystem.material2

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    ),
    h4 = MaterialTheme.typography.h4.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 32.sp,
      lineHeight = 40.sp,
    ),
    h5 = MaterialTheme.typography.h5.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
    ),
    h6 = MaterialTheme.typography.h6.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 20.sp,
      lineHeight = 26.sp,
    ),
    subtitle1 = MaterialTheme.typography.subtitle1.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    subtitle2 = MaterialTheme.typography.subtitle2.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    body1 = MaterialTheme.typography.body1.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    body2 = MaterialTheme.typography.body2.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    button = MaterialTheme.typography.button.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    caption = MaterialTheme.typography.caption.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    overline = MaterialTheme.typography.overline.copy(
      fontWeight = FontWeight.Normal,
      fontSize = 10.sp,
      lineHeight = 16.sp,
    ),
  )

@Preview
@Composable
private fun TypographyPreview() {
  MaterialTheme(typography = HedvigTypography) {
    Column {
      Text("Headline 3", style = MaterialTheme.typography.h3)
      Text("Headline 4", style = MaterialTheme.typography.h4)
      Text("Headline 5", style = MaterialTheme.typography.h5)
      Text("Headline 6", style = MaterialTheme.typography.h6)
      Text("Subtitle 1", style = MaterialTheme.typography.subtitle1)
      Text("Subtitle 2", style = MaterialTheme.typography.subtitle2)
      Text("Body 1", style = MaterialTheme.typography.body1)
      Text("Body 2", style = MaterialTheme.typography.body2)
      Text("Button", style = MaterialTheme.typography.button)
      Text("Caption", style = MaterialTheme.typography.caption)
      Text("Overline", style = MaterialTheme.typography.overline)
    }
  }
}
