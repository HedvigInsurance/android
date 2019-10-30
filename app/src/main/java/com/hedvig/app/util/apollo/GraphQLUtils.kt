package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.getLocale

fun defaultLocale(context: Context) =
    when (getLocale(Language.fromSettings(context)?.apply(context) ?: context).language) {
        "en" -> com.hedvig.android.owldroid.type.Locale.EN_SE
        else -> com.hedvig.android.owldroid.type.Locale.SV_SE
    }
