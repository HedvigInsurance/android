package com.hedvig.android.language

import androidx.annotation.StringRes
import giraffe.type.Locale

enum class Language {
  SV_SE,
  EN_SE,
  NB_NO,
  EN_NO,
  DA_DK,
  EN_DK,
  ;

  val label: Int
    @StringRes
    get() = when (this) {
      SV_SE -> hedvig.resources.R.string.swedish
      EN_SE -> hedvig.resources.R.string.english_swedish
      NB_NO -> hedvig.resources.R.string.norwegian
      EN_NO -> hedvig.resources.R.string.english_norwegian
      DA_DK -> hedvig.resources.R.string.danish
      EN_DK -> hedvig.resources.R.string.english_danish
    }

  override fun toString() = when (this) {
    SV_SE -> SETTING_SV_SE
    EN_SE -> SETTING_EN_SE
    NB_NO -> SETTING_NB_NO
    EN_NO -> SETTING_EN_NO
    DA_DK -> SETTING_DA_DK
    EN_DK -> SETTING_EN_DK
  }

  fun toLocale() = when (this) {
    SV_SE -> Locale.sv_SE
    EN_SE -> Locale.en_SE
    NB_NO -> Locale.nb_NO
    EN_NO -> Locale.en_NO
    DA_DK -> Locale.da_DK
    EN_DK -> Locale.en_DK
  }

  fun webPath() = when (this) {
    SV_SE -> "se"
    EN_SE -> "se-en"
    NB_NO -> "no"
    EN_NO -> "no-en"
    DA_DK -> "dk"
    EN_DK -> "dk-en"
  }

  companion object {
    const val SETTING_SV_SE = "sv-SE"
    const val SETTING_EN_SE = "en-SE"
    const val SETTING_NB_NO = "nb-NO"
    const val SETTING_EN_NO = "en-NO"
    const val SETTING_DA_DK = "da-DK"
    const val SETTING_EN_DK = "en-DK"

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
      else -> throw RuntimeException("Invalid language value: $value")
    }
  }
}
