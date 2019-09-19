package com.hedvig.app.feature.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class Language {
    SWEDISH,
    ENGLISH;

    fun apply(context: Context) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(toLocale())
        context.createConfigurationContext(config)
    }

    fun toLocale() = Locale.forLanguageTag(toString())

    override fun toString() = when (this) {
        SWEDISH -> "sv-SE"
        ENGLISH -> "en-SE"
    }

    companion object {
        fun from(value: String) = when (value) {
            "sv-SE" -> SWEDISH
            "en-SE" -> ENGLISH
            else -> throw Error("Invalid language $value")
        }
    }
}
