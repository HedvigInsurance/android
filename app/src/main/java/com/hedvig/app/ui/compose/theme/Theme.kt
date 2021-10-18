package com.hedvig.app.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

val LocalMarket: ProvidableCompositionLocal<Market?> = staticCompositionLocalOf {
    error("CompositionLocal LocalMarket not present")
}

@Composable
fun HedvigTheme(content: @Composable () -> Unit) {
    val marketManager = getKoin().get<MarketManager>()
    CompositionLocalProvider(LocalMarket provides marketManager.market) {
        MdcTheme(setDefaultFontFamily = true, content = content)
    }
}
