package com.hedvig.app.util

import android.content.Context
import com.hedvig.android.owldroid.graphql.type.Locale
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.getLocale

class LocaleManager(
  private val marketManager: MarketManager,
  private val context: Context,
) {
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
    val localeFromSettings = Language.fromSettings(context, marketManager.market).apply(context)
    return getLocale(localeFromSettings, marketManager.market)
  }
}
