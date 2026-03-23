package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Expect declaration for platform-specific implementation
@Composable
internal expect fun HelipadRiveAnimation(
  modifier: Modifier = Modifier,
  bottomAnimationFinished: Boolean,
  isVisible: Boolean = true,
  stepId: String
)
