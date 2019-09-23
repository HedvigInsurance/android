package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.app.getLocale

fun defaultLocale(context: Context) = when (getLocale(context).language) {
    "en" -> com.hedvig.android.owldroid.type.Locale.EN_SE
    else -> com.hedvig.android.owldroid.type.Locale.SV_SE
}
