package com.hedvig.app.feature.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.preference.PreferenceManager
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.util.extensions.getMarket
import java.util.Locale

enum class Language {
    SV_SE,
    EN_SE,
    NB_NO,
    EN_NO,
    DA_DK,
    EN_DK;

    fun apply(context: Context?): Context? {
        val locale = into()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apply(context, locale)
        } else {
            applySingleLocale(context, locale)
        }
    }

    @Suppress("DEPRECATION")
    private fun applySingleLocale(context: Context?, locale: LocaleWrapper): Context? {
        if (locale !is LocaleWrapper.SingleLocale) {
            throw RuntimeException("Invalid state: API version <= 21 but multiple locales was encountered")
        }
        val unwrappedLocale = locale.locale
        Locale.setDefault(unwrappedLocale)
        if (context == null) {
            return null
        }

        val config = Configuration(context.resources.configuration)
        config.setLocale(unwrappedLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun apply(context: Context?, locale: LocaleWrapper): Context? {
        when (locale) {
            is LocaleWrapper.SingleLocale -> {
                return applySingleLocale(context, locale)
            }
            is LocaleWrapper.MultipleLocales -> {
                val locales = locale.locales
                LocaleList.setDefault(locales)
                if (context == null) {
                    return null
                }
                val config = Configuration(context.resources.configuration)
                config.setLocales(locales)
                return context.createConfigurationContext(config)
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

    override fun toString() = when (this) {
        SV_SE -> SETTING_SV_SE
        EN_SE -> SETTING_EN_SE
        NB_NO -> SETTING_NB_NO
        EN_NO -> SETTING_EN_NO
        DA_DK -> SETTING_DA_DK
        EN_DK -> SETTING_EN_DK
    }

    companion object {
        private const val SETTING_SYSTEM_DEFAULT = "system_default"
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

        fun fromSettings(context: Context?): Language? {
            if (context == null) {
                return null
            }
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val market = context.getMarket()
            if (market != null) {
                val selectedLanguage = sharedPref
                    .getString("language", getAvailableLanguages(market).first().toString())
                    ?: getAvailableLanguages(market).first().toString()
                return if (selectedLanguage == SETTING_SYSTEM_DEFAULT) {
                    from(getAvailableLanguages(market).first().toString())
                } else {
                    from(selectedLanguage)
                }
            }
            return from(SETTING_EN_SE)
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
