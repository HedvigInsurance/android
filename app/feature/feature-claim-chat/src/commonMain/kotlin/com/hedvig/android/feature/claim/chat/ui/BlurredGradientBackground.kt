package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.Res
import hedvig.resources.claim_chat_background_dark
import hedvig.resources.claim_chat_background_light
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BlurredGradientBackground(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Image(
      painter = painterResource(
        if (HedvigTheme.colorScheme.isLight) {
          Res.drawable.claim_chat_background_light
        } else {
          Res.drawable.claim_chat_background_dark
        },
      ),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
  }
}
