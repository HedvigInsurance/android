package com.hedvig.android.language

import androidx.annotation.StringRes
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

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

  fun toBcp47Format(): String {
    return when (this) {
      SV_SE -> BCP_47_SV_SE
      EN_SE -> BCP_47_EN_SE
      NB_NO -> BCP_47_NB_NO
      EN_NO -> BCP_47_EN_NO
      DA_DK -> BCP_47_DA_DK
      EN_DK -> BCP_47_EN_DK
    }
  }

  /**
   * Returns the language string, in BCP-47 format.
   * See [RFC-5646](https://www.rfc-editor.org/info/rfc5646) for more information
   */
  override fun toString(): String {
    return toBcp47Format()
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
    const val BCP_47_SV_SE = "sv-SE"
    const val BCP_47_EN_SE = "en-SE"
    const val BCP_47_NB_NO = "nb-NO"
    const val BCP_47_EN_NO = "en-NO"
    const val BCP_47_DA_DK = "da-DK"
    const val BCP_47_EN_DK = "en-DK"

    /**
     * Parses the language tag, in BCP-47 format, to [Language]
     * See [RFC-5646](https://www.rfc-editor.org/info/rfc5646) for more information
     * @param value: A language tag in BCP-47 format
     */
    fun from(value: String): Language = when (value) {
      BCP_47_SV_SE -> SV_SE
      BCP_47_EN_SE -> EN_SE
      BCP_47_NB_NO -> NB_NO
      BCP_47_EN_NO -> EN_NO
      BCP_47_DA_DK -> DA_DK
      BCP_47_EN_DK -> EN_DK
      else -> EN_SE.also { logcat(LogPriority.WARN) { "Defaulting to EN_SE for language tag: $value" } }
    }
  }
}
