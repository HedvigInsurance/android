package com.hedvig.app.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.android.material.composethemeadapter.MdcTheme
import org.koin.core.context.GlobalContext.get

@Composable
fun HedvigTheme(content: @Composable () -> Unit) {
    val imageLoader = get().get<ImageLoader>()
    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        ProvideWindowInsets {
            MdcTheme(setDefaultFontFamily = true, content = content)
        }
    }
}
