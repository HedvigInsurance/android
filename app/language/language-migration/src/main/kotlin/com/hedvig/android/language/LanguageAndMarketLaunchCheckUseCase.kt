package com.hedvig.android.language

import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase
import java.util.Locale
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
    val currentLanguageList = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: initial language: $currentLanguageList" }

    //todo: remove comments here
    //first ever launch

    val systemLocale = Locale.getDefault() //some-English
    val systemLanguage = Language.from(systemLocale.toLanguageTag()) //EN_SE

    val currentLanguage = languageService.getSelectedLanguage() //null

    val market = marketManager.selectedMarket().first() //null

    val currentLanguageMatchesMarket = if (market == null) {
      false
    } else {
      currentLanguage in market.availableLanguages
    }
    // Set the market again, which sets the right language too if there is no language selected or no market selected
    if (currentLanguageList.isEmpty || market == null)
    {
      setMarketUseCase.setMarket(
        (market ?: Market.SE).also {
          logcat { "LanguageAndMarketLaunchCheckUseCase: currentLanguageList.isEmpty: ${currentLanguageList.isEmpty}, " +
            "setting market to $it" }
        },
      )
    } else if (!currentLanguageMatchesMarket) {
      setMarketUseCase.setMarket(
        (market).also {
          logcat { "LanguageAndMarketLaunchCheckUseCase: currentLanguageMatchesMarket is false, setting market to $it" }
        },
      )
    }
    val currentLanguageListAfter = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: after check language: $currentLanguageListAfter" }
  }
}
