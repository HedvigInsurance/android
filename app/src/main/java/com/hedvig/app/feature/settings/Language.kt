package com.hedvig.app.feature.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.preference.PreferenceManager
import java.util.Locale

enum class Language {
    SYSTEM_DEFAULT,
    SV_SE,
    EN_SE,
    NB_NO,
    EN_NO;

    fun apply(context: Context?): Context? {
        val locale = into()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apply(context, locale)
        } else {
            applySingleLocale(context, locale)
        }
    }

    @SuppressWarnings("deprecation")
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
        SYSTEM_DEFAULT -> DefaultLocale.get()
    }

    override fun toString() = when (this) {
        SYSTEM_DEFAULT -> SETTING_SYSTEM_DEFAULT
        SV_SE -> SETTING_SV_SE
        EN_SE -> SETTING_EN_SE
        NB_NO -> SETTING_NB_NO
        EN_NO -> SETTING_EN_NO
    }

    companion object {
        const val SETTING_SYSTEM_DEFAULT = "system_default"
        const val SETTING_SV_SE = "sv-SE"
        const val SETTING_EN_SE = "en-SE"
        const val SETTING_NB_NO = "nb-NO"
        const val SETTING_EN_NO = "en-NO"

        fun from(value: String) = when (value) {
            SETTING_SYSTEM_DEFAULT -> SYSTEM_DEFAULT
            SETTING_SV_SE -> SV_SE
            SETTING_EN_SE -> EN_SE
            SETTING_NB_NO -> NB_NO
            SETTING_EN_NO -> EN_NO
            else -> throw RuntimeException("Invalid language value: $value")
        }

        fun fromSettings(context: Context?): Language? {
            if (context == null) {
                return null
            }

            return from(
                PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString("language", SETTING_SYSTEM_DEFAULT)
                    ?: SETTING_SYSTEM_DEFAULT
            )
        }
    }

    object DefaultLocale {
        private var locale: LocaleWrapper? = null

        internal fun get(): LocaleWrapper {
            return locale ?: throw RuntimeException("DefaultLocale has not been initialized")
        }

        fun initialize() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.locale = LocaleWrapper.MultipleLocales(LocaleList.getDefault())
                return
            } else {
                this.locale = LocaleWrapper.SingleLocale(Locale.getDefault())
            }
        }
    }

    sealed class LocaleWrapper {
        data class SingleLocale(val locale: Locale) : LocaleWrapper()
        data class MultipleLocales(val locales: LocaleList) : LocaleWrapper()
    }
}
