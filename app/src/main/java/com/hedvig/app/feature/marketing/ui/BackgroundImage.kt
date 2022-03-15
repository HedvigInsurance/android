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
import com.hedvig.app.util.compose.rememberBlurHash

@Composable
fun BackgroundImage(background: Background, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        (background as? Background.Loaded)?.let { bg ->
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.statusBarDarkContentEnabled = when (bg.theme) {
                    Background.Theme.LIGHT -> false
                    Background.Theme.DARK -> true
                }
            }
            val placeholder by rememberBlurHash(bg.blurHash, 32, 32)
            Image(
                painter = rememberImagePainter(
                    data = bg.url,
                    builder = {
                        placeholder(placeholder)
                        crossfade(true)
                        scale(Scale.FILL)
                    }
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            content()
        }
    }
}
