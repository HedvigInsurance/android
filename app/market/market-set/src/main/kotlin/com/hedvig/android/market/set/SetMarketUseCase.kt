package com.hedvig.android.market.set

import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.InternalHedvigMarketApi
import com.hedvig.android.market.InternalSetMarketUseCase
import com.hedvig.android.market.Market

/**
 * Sets the market, and ensures that the language chosen from [LanguageService] is also a valid one for that market.
 * If English was chosen before, it ensures that the english choice is persisted, otherwise the appropriate local
 * language is picked instead.
 */
interface SetMarketUseCase {
  suspend fun setMarket(market: Market)
}

@OptIn(InternalHedvigMarketApi::class)
internal class SetMarketUseCaseImpl(
  private val internalSetMarketUseCase: InternalSetMarketUseCase,
  private val languageService: LanguageService,
) : SetMarketUseCase {
  override suspend fun setMarket(market: Market) {
    val existingLanguage = languageService.getSelectedLanguage()
    logcat { "SetMarketUseCase setting market to $market, existing language is $existingLanguage" }
    if (existingLanguage !in market.availableLanguages) {
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

private val englishLanguageList = listOf(Language.EN_SE, Language.EN_DK, Language.EN_NO, Language.EN_GLOBAL)

private fun Market.localLanguage() = when (this) {
  Market.SE -> Language.SV_SE
  Market.NO -> Language.NB_NO
  Market.DK -> Language.DA_DK
}

private fun Market.englishLanguage() = when (this) {
  Market.SE -> Language.EN_SE
  Market.NO -> Language.EN_NO
  Market.DK -> Language.EN_DK
}
