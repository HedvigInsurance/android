package com.hedvig.app.feature.marketing.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

class UpdateApplicationLanguageUseCase(
  private val marketManager: MarketManager,
) {
  operator fun invoke(market: Market, language: Language) {
    marketManager.market = market
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.toString()))
  }
}
