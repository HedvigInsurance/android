package com.hedvig.android.market

import com.hedvig.android.language.Language

enum class Market {
  SE,
  ;

  val availableLanguages: List<Language>
    get() = when (this) {
      Market.SE -> listOf(Language.SV_SE, Language.EN_SE)
    }
}
