package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.theme.SansStandard

internal val HedvigTypography: Typography
  @Composable
  get() = Typography(
    displayLarge = MaterialTheme.typography.displayLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 57.sp,
      lineHeight = 64.sp,
    ),
    displayMedium = MaterialTheme.typography.displayMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 45.sp,
      lineHeight = 52.sp,
    ),
    displaySmall = MaterialTheme.typography.displaySmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 36.sp,
      lineHeight = 44.sp,
    ),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 32.sp,
      lineHeight = 40.sp,
    ),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 28.sp,
      lineHeight = 36.sp,
    ),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
    ),
    titleLarge = MaterialTheme.typography.titleLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 22.sp,
      lineHeight = 28.sp,
    ),
    titleMedium = MaterialTheme.typography.titleMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    titleSmall = MaterialTheme.typography.titleSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    bodySmall = MaterialTheme.typography.bodySmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    labelLarge = MaterialTheme.typography.labelLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    labelMedium = MaterialTheme.typography.labelMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    labelSmall = MaterialTheme.typography.labelSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 11.sp,
      lineHeight = 16.sp,
    ),
  )

@Preview
@Composable
private fun TypographyPreview() {
  MaterialTheme(typography = HedvigTypography) {
    Column {
      Text("Display Large", style = MaterialTheme.typography.displayLarge)
      Text("Display Medium", style = MaterialTheme.typography.displayMedium)
      Text("Display Small", style = MaterialTheme.typography.displaySmall)
      Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
      Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
      Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
      Text("Title Large", style = MaterialTheme.typography.titleLarge)
      Text("Title Medium", style = MaterialTheme.typography.titleMedium)
      Text("Title Small", style = MaterialTheme.typography.titleSmall)
      Text("Body Large", style = MaterialTheme.typography.bodyLarge)
      Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
      Text("Body Small", style = MaterialTheme.typography.bodySmall)
      Text("Label Large", style = MaterialTheme.typography.labelLarge)
      Text("Label Medium", style = MaterialTheme.typography.labelMedium)
      Text("Label Small", style = MaterialTheme.typography.labelSmall)
    }
  }
}
