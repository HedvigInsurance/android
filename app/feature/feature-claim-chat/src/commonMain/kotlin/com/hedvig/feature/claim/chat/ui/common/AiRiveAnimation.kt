package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Expect declaration for platform-specific implementation
@Composable
internal expect fun AiRiveAnimation(isAnimationComplete: Boolean, modifier: Modifier = Modifier)
