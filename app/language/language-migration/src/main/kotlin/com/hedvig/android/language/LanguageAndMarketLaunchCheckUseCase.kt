package com.hedvig.android.language

import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase
import kotlinx.coroutines.flow.first

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
    val currentLanguage = languageService.getSelectedLanguage()
    logcat { "LanguageAndMarketLaunchCheckUseCase: currentLanguage: $currentLanguage" }

    val market = marketManager.selectedMarket().first()

    val currentLanguageMatchesMarket = if (market == null || currentLanguage == null) {
      false
    } else {
      currentLanguage in market.availableLanguages
    }
    // Set the market again, which sets the right language too if there is no language selected or no market selected
    if (currentLanguage == null) {
      setMarketUseCase.setMarket(
        preferSystemLanguageIfExistingIsNull = true,
        market = (market ?: Market.SE).also {
          logcat {
            "LanguageAndMarketLaunchCheckUseCase: currentLanguage is null, " +
              "market is: $market, setting market to $it with preferSystemLanguageIfExistingIsNull as true"
          }
        },
      )
    } else if (market == null || !currentLanguageMatchesMarket) {
      setMarketUseCase.setMarket(
        market = (market ?: Market.SE).also {
          logcat {
            "LanguageAndMarketLaunchCheckUseCase: currentLanguageMatchesMarket is false, " +
              "market is: $market, setting market to $it"
          }
        },
      )
    }
  }
}
