package com.hedvig.android.core.designsystem.material3

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.theme.SansStandard

internal val HedvigTypography: Typography
  @Composable
  get() = Typography(
    displayLarge = MaterialTheme.typography.displayLarge.copy(
      fontFamily = SansStandard,
      fontSize = 57.sp,
      lineHeight = 64.sp,
      letterSpacing = (-1.0).sp,
    ),
    displayMedium = MaterialTheme.typography.displayMedium.copy(
      fontFamily = SansStandard,
      fontSize = 45.sp,
      lineHeight = 52.sp,
      letterSpacing = (-0.5).sp,
    ),
    displaySmall = MaterialTheme.typography.displaySmall.copy(
      fontFamily = SansStandard,
      fontSize = 36.sp,
      lineHeight = 44.sp,
      letterSpacing = (-0.5).sp,
    ),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(
      fontFamily = SansStandard,
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = (-0.25).sp,
    ),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(
      fontFamily = SansStandard,
      fontSize = 28.sp,
      lineHeight = 36.sp,
      letterSpacing = 0.sp,
    ),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(
      fontFamily = SansStandard,
      fontSize = 24.sp,
      lineHeight = 32.sp,
      letterSpacing = 0.sp,
    ),
    titleLarge = MaterialTheme.typography.titleLarge.copy(
      fontFamily = SansStandard,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.sp,
    ),
    titleMedium = MaterialTheme.typography.titleMedium.copy(
      fontFamily = SansStandard,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15.sp,
    ),
    titleSmall = MaterialTheme.typography.titleSmall.copy(
      fontFamily = SansStandard,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp,
    ),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(
      fontFamily = SansStandard,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15.sp,
    ),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(
      fontFamily = SansStandard,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp,
    ),
    bodySmall = MaterialTheme.typography.bodySmall.copy(
      fontFamily = SansStandard,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.2.sp,
    ),
    labelLarge = MaterialTheme.typography.labelLarge.copy(
      fontFamily = SansStandard,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp,
    ),
    labelMedium = MaterialTheme.typography.labelMedium.copy(
      fontFamily = SansStandard,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp,
    ),
    labelSmall = MaterialTheme.typography.labelSmall.copy(
      fontFamily = SansStandard,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp,
    ),
  )
