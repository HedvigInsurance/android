package com.hedvig.app.feature.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.hedvig.app.R
import java.util.Locale

enum class Language {
    SV_SE,
    EN_SE,
    NB_NO,
    EN_NO,
    DA_DK,
    EN_DK;

    fun apply(context: Context): Context {
        val locale = into()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apply(context, locale)
        } else {
            applySingleLocale(context, locale)
        }
    }

    @Suppress("DEPRECATION")
    private fun applySingleLocale(context: Context, locale: LocaleWrapper): Context {
        if (locale !is LocaleWrapper.SingleLocale) {
            throw RuntimeException("Invalid state: API version <= 21 but multiple locales was encountered")
        }
        val unwrappedLocale = locale.locale
        Locale.setDefault(unwrappedLocale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(unwrappedLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun apply(context: Context, locale: LocaleWrapper): Context {
        return when (locale) {
            is LocaleWrapper.SingleLocale -> {
                applySingleLocale(context, locale)
            }
            is LocaleWrapper.MultipleLocales -> {
                val locales = locale.locales
                LocaleList.setDefault(locales)
                val config = Configuration(context.resources.configuration)
                config.setLocales(locales)
                context.createConfigurationContext(config)
            }
        }
    }

    private fun into(): LocaleWrapper = when (this) {
        SV_SE -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_SV_SE))
        EN_SE -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_EN_SE))
        NB_NO -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_NB_NO))
        EN_NO -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_EN_NO))
        DA_DK -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_DA_DK))
        EN_DK -> LocaleWrapper.SingleLocale(Locale.forLanguageTag(SETTING_EN_DK))
    }

    fun getLabel() = when (this) {
        SV_SE -> R.string.swedish
        EN_SE -> R.string.english_swedish
        NB_NO -> R.string.norwegian
        EN_NO -> R.string.english_norwegian
        DA_DK -> R.string.danish
        EN_DK -> R.string.english_danish
    }

    /**
     * Returns the IETF BCP 47 language tag string for the current [Language]
     */
    fun getLanguageTag(): String = when (this) {
        SV_SE -> "sv-SE"
        EN_SE -> "en-SE"
        NB_NO -> "nb-NO"
        EN_NO -> "en-NO"
        DA_DK -> "da-DK"
        EN_DK -> "en-DK"
    }

    override fun toString() = when (this) {
        SV_SE -> SETTING_SV_SE
        EN_SE -> SETTING_EN_SE
        NB_NO -> SETTING_NB_NO
        EN_NO -> SETTING_EN_NO
        DA_DK -> SETTING_DA_DK
        EN_DK -> SETTING_EN_DK
    }

    companion object {
        const val SETTING_SYSTEM_DEFAULT = "system_default"
        const val SETTING_SV_SE = "sv-SE"
        const val SETTING_EN_SE = "en-SE"
        const val SETTING_NB_NO = "nb-NO"
        const val SETTING_EN_NO = "en-NO"
        const val SETTING_DA_DK = "da-DK"
        const val SETTING_EN_DK = "en-DK"

        fun from(value: String) = when (value) {
            SETTING_SV_SE -> SV_SE
            SETTING_EN_SE -> EN_SE
            SETTING_NB_NO -> NB_NO
            SETTING_EN_NO -> EN_NO
            SETTING_DA_DK -> DA_DK
            SETTING_EN_DK -> EN_DK
            else -> throw RuntimeException("Invalid language value: $value")
        }

        fun persist(context: Context, language: Language) {
            PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
                putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
            }
        }

        fun fromSettings(context: Context, market: Market?): Language = when (market) {
            null -> from(SETTING_EN_SE)
            else -> getLanguageFromSharedPreferences(context, market)
        }

        private fun getLanguageFromSharedPreferences(context: Context, market: Market): Language {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val firstAvailableLanguage = getAvailableLanguages(market).first().toString()
            val selectedLanguage = sharedPref.getString("language", firstAvailableLanguage) ?: firstAvailableLanguage

            return if (selectedLanguage == SETTING_SYSTEM_DEFAULT) {
                from(firstAvailableLanguage)
            } else {
                from(selectedLanguage)
            }
        }

        fun getAvailableLanguages(market: Market): List<Language> {
            return when (market) {
                Market.SE -> listOf(SV_SE, EN_SE)
                Market.NO -> listOf(NB_NO, EN_NO)
                Market.DK -> listOf(DA_DK, EN_DK)
            }
        }
    }

    sealed class LocaleWrapper {
        data class SingleLocale(val locale: Locale) : LocaleWrapper()
        data class MultipleLocales(val locales: LocaleList) : LocaleWrapper()
    }
}
