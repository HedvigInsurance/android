package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun BlurredGradientBackground(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {}
}
