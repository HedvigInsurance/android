package com.hedvig.android.market.set

import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.InternalHedvigMarketApi
import com.hedvig.android.market.InternalSetMarketUseCase
import com.hedvig.android.market.Market
import java.util.Locale

/**
 * Sets the market, and ensures that the language chosen from [LanguageService] is also a valid one for that market.
 * If English was chosen before, it ensures that the english choice is persisted, otherwise the appropriate local
 * language is picked instead.
 */
interface SetMarketUseCase {
  suspend fun setMarket(market: Market, preferSystemDefaultLocale: Locale? = null)
}

@OptIn(InternalHedvigMarketApi::class)
internal class SetMarketUseCaseImpl(
  private val internalSetMarketUseCase: InternalSetMarketUseCase,
  private val languageService: LanguageService,
) : SetMarketUseCase {
  override suspend fun setMarket(market: Market, preferSystemDefaultLocale: Locale?) {
    val selectedLanguage = languageService.getSelectedLanguage()
    val existingLanguage = if (preferSystemDefaultLocale == null) {
      selectedLanguage
    } else {
      selectedLanguage ?: Language.from(preferSystemDefaultLocale.toLanguageTag())
    }
    logcat {
      "SetMarketUseCase setting market to $market, " +
        "selectedLanguage is $selectedLanguage, " +
        "preferSystemDefaultLocale is: $preferSystemDefaultLocale, " +
        "existing language is $existingLanguage,"
    }
    if (selectedLanguage == null || existingLanguage !in market.availableLanguages) {
      languageService.setLanguage(
        when {
          existingLanguage == null -> market.localLanguage()
          existingLanguage.isEnglishLanguage() -> market.englishLanguage()
          else -> market.localLanguage()
        }.also {
          logcat { "SetMarketUseCase setting language to $it" }
        },
      )
    }
    internalSetMarketUseCase.setMarket(market)
  }
}

private fun Language.isEnglishLanguage(): Boolean {
  return this in englishLanguageList
}

// TODO: MarketCleanup
private val englishLanguageList = listOf(Language.EN_SE)

private fun Market.localLanguage() = when (this) {
  Market.SE -> Language.SV_SE
}

private fun Market.englishLanguage() = when (this) {
  Market.SE -> Language.EN_SE
}
