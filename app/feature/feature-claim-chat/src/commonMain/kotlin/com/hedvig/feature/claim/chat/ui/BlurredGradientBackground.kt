package com.hedvig.feature.claim.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun BlurredGradientBackground(
  modifier: Modifier = Modifier,
  radius: Int = 100,
)
