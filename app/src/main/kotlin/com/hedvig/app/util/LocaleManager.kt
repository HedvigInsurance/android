package com.hedvig.app.util

import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.apollo.graphql.type.Locale

class LocaleManager {
  fun defaultLocale(): Locale {
    val locale = getJavaUtilLocale()
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

  fun getJavaUtilLocale(): java.util.Locale {
    val localeList = AppCompatDelegate.getApplicationLocales()
    if (localeList.isEmpty) {
      return java.util.Locale("en", "SE")
    }

    return localeList[0]!!
  }
}
