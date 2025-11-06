package com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformBlurContainer(
  modifier: Modifier = Modifier,
  radius: Int = 100,
  content: @Composable () -> Unit
)
