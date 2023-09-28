package com.hedvig.app.feature.marketing.data

import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

// TODO delete this class and use defaults from MarketManager for the new login screens
class GetInitialMarketPickerValuesUseCase(
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  suspend operator fun invoke(): Pair<Market, Language> {
    val currentMarket = marketManager.market.value
    val currentLanguage = languageService.getLanguage()
    return currentMarket to currentLanguage
  }
}
