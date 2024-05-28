package com.hedvig.android.market

import androidx.annotation.StringRes
import com.hedvig.android.language.Language

enum class Market {
  SE,
  NO,
  DK,
  ;

  val label: Int
    @StringRes
    get() = when (this) {
      SE -> hedvig.resources.R.string.market_sweden
      NO -> hedvig.resources.R.string.market_norway
      DK -> hedvig.resources.R.string.market_denmark
    }

  val availableLanguages: List<Language>
    get() = when (this) {
      Market.SE -> listOf(Language.SV_SE, Language.EN_SE)
      Market.NO -> listOf(Language.NB_NO, Language.EN_NO)
      Market.DK -> listOf(Language.DA_DK, Language.EN_DK)
    }
}
