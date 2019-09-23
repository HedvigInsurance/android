package com.hedvig.app.feature.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

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
        LIGHT -> "light"
        DARK -> "dark"
        SYSTEM_DEFAULT -> "system_default"
    }

    companion object {
        fun from(value: String) = when (value) {
            "light" -> LIGHT
            "dark" -> DARK
            "system_default" -> SYSTEM_DEFAULT
            else -> throw RuntimeException("Invalid theme value: $value")
        }
    }
}
