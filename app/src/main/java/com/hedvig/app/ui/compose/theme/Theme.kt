package com.hedvig.app.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import org.koin.java.KoinJavaComponent.getKoin

val LocalMarket: ProvidableCompositionLocal<Market?> = staticCompositionLocalOf {
    error("CompositionLocal LocalMarket not present")
}

val LocalLanguage: ProvidableCompositionLocal<Language> = staticCompositionLocalOf {
    error("CompositionLocal LocalMarket not present")
}

@Composable
fun HedvigTheme(content: @Composable () -> Unit) {
    val marketManager = getKoin().get<MarketManager>()
    val market = marketManager.market
    val language = Language.fromSettings(LocalContext.current, market)
    CompositionLocalProvider(
        LocalMarket provides market,
        LocalLanguage provides language,
    ) {
        MdcTheme(setDefaultFontFamily = true, content = content)
    }
}
