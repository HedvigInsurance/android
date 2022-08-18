package com.hedvig.app.feature.marketing.ui

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hedvig.android.core.designsystem.theme.hedvigBlack
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.util.compose.blurHash
import com.hedvig.app.util.compose.toPainter

@Composable
fun BackgroundImage(background: MarketingBackground?, imageLoader: ImageLoader) {
  val backgroundImageState = rememberBackgroundImageState(background)
  val systemUiController = rememberSystemUiController()
  SideEffect {
    if (backgroundImageState.theme != null) {
      systemUiController.statusBarDarkContentEnabled = when (backgroundImageState.theme) {
        MarketingBackground.Theme.LIGHT -> false
        MarketingBackground.Theme.DARK -> true
      }
    } else {
      // Black background as a fallback means we want the status bar items to be light
      systemUiController.statusBarDarkContentEnabled = false
    }
  }
  AsyncImage(
    model = backgroundImageState.data,
    contentDescription = null,
    imageLoader = imageLoader,
    contentScale = ContentScale.Crop,
    placeholder = backgroundImageState.fallbackPainter,
    error = backgroundImageState.fallbackPainter,
    fallback = backgroundImageState.fallbackPainter,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun rememberBackgroundImageState(
  background: MarketingBackground?,
  context: Context = LocalContext.current,
): BackgroundImageUiState {
  return remember(background, context) { BackgroundImageUiState(background, context) }
}

private class BackgroundImageUiState private constructor(
  val data: Any?,
  val fallbackPainter: Painter?, // Painter for any case where the image can not be loaded.
  val theme: MarketingBackground.Theme?,
) {
  companion object {
    operator fun invoke(marketingBackground: MarketingBackground?, context: Context): BackgroundImageUiState {
      if (marketingBackground == null) {
        return BackgroundImageUiState(null, ColorPainter(hedvigBlack), null)
      }

      val blurHashDrawable: BitmapDrawable? = blurHash(marketingBackground.blurHash, 32, 32, context)
      val fallbackPainter = blurHashDrawable?.toPainter() ?: ColorPainter(hedvigBlack)
      return BackgroundImageUiState(
        data = marketingBackground.url,
        fallbackPainter = fallbackPainter,
        theme = marketingBackground.theme,
      )
    }
  }
}
