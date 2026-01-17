package com.hedvig.feature.claim.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.Res
import hedvig.resources.blur_background
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BlurredGradientBackground(modifier: Modifier = Modifier) {
  val isDarkTheme = isSystemInDarkTheme()
  if (isDarkTheme) {
    Surface(modifier = modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary){}
  } else {
    Image(
      painter = painterResource(Res.drawable.blur_background),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = modifier.fillMaxSize()
    )
  }

}
