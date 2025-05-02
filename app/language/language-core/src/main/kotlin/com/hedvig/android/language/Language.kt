package com.hedvig.android.language

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

enum class Language {
  SV_SE,
  EN_SE,
  ;

  fun toBcp47Format(): String {
    return when (this) {
      SV_SE -> BCP_47_SV_SE
      EN_SE -> BCP_47_EN_SE
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
  }

  companion object {
    const val BCP_47_SV_SE = "sv-SE"
    const val BCP_47_EN_SE = "en-SE"
    const val BCP_47_SV = "sv"
    const val BCP_47_EN = "en"
    const val BCP_47_SV_FOR_SUBSTRING = "sv-"
    const val BCP_47_EN_FOR_SUBSTRING = "en-"

    /**
     * Parses the language tag, in BCP-47 format, to [Language]
     * See [RFC-5646](https://www.rfc-editor.org/info/rfc5646) for more information
     * @param value: A language tag in BCP-47 format
     */
    fun from(value: String): Language = when {
      value == BCP_47_SV_SE -> SV_SE
      value == BCP_47_EN_SE -> EN_SE
      value == BCP_47_EN || value.startsWithSubstring(BCP_47_EN_FOR_SUBSTRING) -> {
        EN_SE.also { logcat(LogPriority.WARN) { "Mapping to EN_SE for language tag: $value" } }
      }

      value == BCP_47_SV || value.startsWithSubstring(BCP_47_SV_FOR_SUBSTRING) -> {
        SV_SE.also { logcat(LogPriority.WARN) { "Mapping to SV_SE for language tag: $value" } }
      }

      else -> EN_SE.also { logcat(LogPriority.WARN) { "Defaulting to EN_SE for language tag: $value" } }
    }

    private fun String.startsWithSubstring(subString: String): Boolean {
      return this.length >= 4 && this.substring(0, 3) == subString
    }
  }
}
