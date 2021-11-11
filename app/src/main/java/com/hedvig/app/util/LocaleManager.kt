package com.hedvig.app.util

import android.content.Context
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.getLocale

class LocaleManager(
    private val marketManager: MarketManager,
    private val context: Context
) {
    fun getLanguageTag(): String {
        return Language.fromSettings(context, marketManager.market).getLanguageTag()
    }

    fun defaultLocale(): Locale {
        val localeFromSettings = Language.fromSettings(context, marketManager.market).apply(context)
        val locale = getLocale(localeFromSettings, marketManager.market)
        return when (locale.toString()) {
            "en_NO" -> Locale.EN_NO
            "nb_NO" -> Locale.NB_NO
            "sv_SE" -> Locale.SV_SE
            "en_SE" -> Locale.EN_SE
            "da_DK" -> Locale.DA_DK
            "en_DK" -> Locale.EN_DK
            else -> Locale.EN_SE
        }
    }
}
