package com.hedvig.android.market

import giraffe.type.Locale

enum class Language {
  SV_SE,
  EN_SE,
  NB_NO,
  EN_NO,
  DA_DK,
  EN_DK,
  FR_FR,
  EN_FR,
  ;

  fun getLabel() = when (this) {
    SV_SE -> hedvig.resources.R.string.swedish
    EN_SE -> hedvig.resources.R.string.english_swedish
    NB_NO -> hedvig.resources.R.string.norwegian
    EN_NO -> hedvig.resources.R.string.english_norwegian
    DA_DK -> hedvig.resources.R.string.danish
    EN_DK -> hedvig.resources.R.string.english_danish
    FR_FR -> hedvig.resources.R.string.french
    EN_FR -> hedvig.resources.R.string.english_french
  }

  override fun toString() = when (this) {
    SV_SE -> SETTING_SV_SE
    EN_SE -> SETTING_EN_SE
    NB_NO -> SETTING_NB_NO
    EN_NO -> SETTING_EN_NO
    DA_DK -> SETTING_DA_DK
    EN_DK -> SETTING_EN_DK
    FR_FR -> SETTING_FR_FR
    EN_FR -> SETTING_EN_FR
  }

  fun toLocale() = when (this) {
    SV_SE -> Locale.sv_SE
    EN_SE -> Locale.en_SE
    NB_NO -> Locale.nb_NO
    EN_NO -> Locale.en_NO
    DA_DK -> Locale.da_DK
    EN_DK -> Locale.en_DK
    // Default to `en_SE` while FR-locales are not available
    else -> Locale.en_SE
  }

  fun webPath() = when (this) {
    SV_SE -> "se"
    EN_SE -> "se-en"
    NB_NO -> "no"
    EN_NO -> "no-en"
    DA_DK -> "dk"
    EN_DK -> "dk-en"
    FR_FR -> "fr"
    EN_FR -> "fr-en"
  }

  companion object {
    const val SETTING_SYSTEM_DEFAULT = "system_default"
    const val SETTING_SV_SE = "sv-SE"
    const val SETTING_EN_SE = "en-SE"
    const val SETTING_NB_NO = "nb-NO"
    const val SETTING_EN_NO = "en-NO"
    const val SETTING_DA_DK = "da-DK"
    const val SETTING_EN_DK = "en-DK"
    const val SETTING_FR_FR = "fr-FR"
    const val SETTING_EN_FR = "en-FR"

    /**
     * Parses the language tag, in BCP-47 format, to [Language]
     * See [RFC-5646](https://www.rfc-editor.org/info/rfc5646) for more information
     * @param value: A language tag in BCP-47 format
     */
    fun from(value: String): Language = when (value) {
      SETTING_SV_SE -> SV_SE
      SETTING_EN_SE -> EN_SE
      SETTING_NB_NO -> NB_NO
      SETTING_EN_NO -> EN_NO
      SETTING_DA_DK -> DA_DK
      SETTING_EN_DK -> EN_DK
      SETTING_FR_FR -> FR_FR
      SETTING_EN_FR -> EN_FR
      else -> throw RuntimeException("Invalid language value: $value")
    }

    fun getAvailableLanguages(market: Market): List<Language> {
      return when (market) {
        Market.SE -> listOf(SV_SE, EN_SE)
        Market.NO -> listOf(NB_NO, EN_NO)
        Market.DK -> listOf(DA_DK, EN_DK)
        Market.FR -> listOf(FR_FR, EN_FR)
      }
    }
  }
}
