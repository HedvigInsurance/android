package com.hedvig.app.util.apollo

import android.content.Context
import com.hedvig.app.getLocale

fun defaultLocale(context: Context) = when (getLocale(context).language) {
    "en" -> type.Locale.EN_SE
    else -> type.Locale.SV_SE
}
