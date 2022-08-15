package com.hedvig.app.feature.marketing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hedvig.app.feature.marketing.Background
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.util.compose.rememberBlurHash

@Composable
fun BackgroundImage(background: Background, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = Modifier.fillMaxSize()) {
    background.data?.let { bg ->
      val systemUiController = rememberSystemUiController()
      SideEffect {
        systemUiController.statusBarDarkContentEnabled = when (bg.theme) {
          MarketingBackground.Theme.LIGHT -> false
          MarketingBackground.Theme.DARK -> true
        }
      }
      rememberSystemUiController()
      Image(
        painter = rememberImagePainter(
          data = bg.url,
          builder = {
            placeholder(rememberBlurHash(marketingBackground.blurHash, 32, 32))
            crossfade(true)
            scale(Scale.FILL)
          },
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
    }
    content()
  }
}
