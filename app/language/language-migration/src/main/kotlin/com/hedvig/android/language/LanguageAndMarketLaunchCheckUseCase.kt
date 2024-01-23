package com.hedvig.android.language

import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase

/**
 * Runs once on app startup to ensure that the language from the current market is properly set.
 * https://developer.android.com/guide/topics/resources/app-languages#impl-overview
 */
interface LanguageAndMarketLaunchCheckUseCase {
  suspend fun invoke()
}

internal class AndroidLanguageAndMarketLaunchCheckUseCase(
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
  private val setMarketUseCase: SetMarketUseCase,
) : LanguageAndMarketLaunchCheckUseCase {
  override suspend fun invoke() {
    val currentLanguageList = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: initial language: $currentLanguageList" }
    val currentLanguage = languageService.getLanguage()
    val market = marketManager.market()
    val currentLanguageMatchesMarket = if (market == null) {
      true
    } else {
      currentLanguage in market.availableLanguages
    }
    // Set the market again, which sets the right language too if there is no language selected or no market selected
    if (currentLanguageList.isEmpty || market == null || !currentLanguageMatchesMarket) {
      setMarketUseCase.setMarket(
        (market ?: Market.SE).also {
          logcat { "LanguageAndMarketLaunchCheckUseCase: setting market to $it" }
        },
      )
    }
    val currentLanguageListAfter = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: after check language: $currentLanguageListAfter" }
  }
}
