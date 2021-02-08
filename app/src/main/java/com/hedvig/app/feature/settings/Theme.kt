package com.hedvig.app.feature.settings

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM_DEFAULT;

    fun apply() {
        when (this) {
            LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SYSTEM_DEFAULT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    override fun toString() = when (this) {
        LIGHT -> SETTING_LIGHT
        DARK -> SETTING_DARK
        SYSTEM_DEFAULT -> SETTING_SYSTEM_DEFAULT
    }

    companion object {
        const val SETTING_SYSTEM_DEFAULT = "system_default"
        const val SETTING_LIGHT = "light"
        const val SETTING_DARK = "dark"

        fun from(value: String) = when (value) {
            SETTING_LIGHT -> LIGHT
            SETTING_DARK -> DARK
            SETTING_SYSTEM_DEFAULT -> SYSTEM_DEFAULT
            else -> throw RuntimeException("Invalid theme value: $value")
        }

        fun fromSettings(context: Context?): Theme? {
            if (context == null) {
                return null
            }

            return from(
                PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString("theme", SETTING_SYSTEM_DEFAULT)
                    ?: SETTING_SYSTEM_DEFAULT
            )
        }
    }
}
