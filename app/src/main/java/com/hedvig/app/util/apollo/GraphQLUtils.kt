package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.getLocale

fun defaultLocale(context: Context) =
    when (getLocale(Language.fromSettings(context)?.apply(context) ?: context).toString()) {
        "en_NO" -> Locale.EN_NO
        "nb_NO" -> Locale.NB_NO
        "sv_SE" -> Locale.SV_SE
        "en_SE" -> Locale.EN_SE
        else -> Locale.EN_SE
    }

fun Locale.toLocaleString() = when (this) {
    Locale.EN_SE -> "en_SE"
    Locale.SV_SE -> "sv_SE"
    Locale.EN_NO -> "en_NO"
    Locale.NB_NO -> "nb_NO"
    Locale.UNKNOWN__ -> ""
}

fun Locale.toWebLocaleTag() = when (this) {
    Locale.EN_SE -> "se-en"
    Locale.SV_SE -> "se"
    Locale.NB_NO -> "no"
    Locale.EN_NO -> "no-en"
    Locale.UNKNOWN__ -> "se-en"
}
