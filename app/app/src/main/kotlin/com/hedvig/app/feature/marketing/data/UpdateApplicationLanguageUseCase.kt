package com.hedvig.app.feature.marketing.data

import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

// todo delete this and set market and language separately, while still maintaining the compatibility between market
//  and languge pairs that are allowed
class UpdateApplicationLanguageUseCase(
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  suspend fun invoke(market: Market, language: Language) {
    marketManager.setMarket(market)
    languageService.setLanguage(language)
    logcat(LogPriority.INFO) { "Set market to:$market and language to:$language" }
  }
}
