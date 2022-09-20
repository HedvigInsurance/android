package com.hedvig.app.util

import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.language.LanguageService

class GraphQLLocaleService(
  private val languageService: LanguageService,
) {
  @Deprecated("don't")
  fun defaultLocale(): Locale {
    val locale = languageService.getLocale()
    return when (locale.toString()) {
      "en_NO" -> Locale.en_NO
      "nb_NO" -> Locale.nb_NO
      "sv_SE" -> Locale.sv_SE
      "en_SE" -> Locale.en_SE
      "da_DK" -> Locale.da_DK
      "en_DK" -> Locale.en_DK
      else -> Locale.en_SE
    }
  }
}
