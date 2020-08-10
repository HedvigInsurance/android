package com.hedvig.app.feature.loggedin.ui

import androidx.annotation.IdRes
import com.hedvig.app.R

enum class LoggedInTabs {
    HOME,
    INSURANCE,
    KEY_GEAR,
    REFERRALS,
    PROFILE;

    @IdRes
    fun id() = when (this) {
        HOME -> R.id.home
        INSURANCE -> R.id.insurance
        KEY_GEAR -> R.id.key_gear
        REFERRALS -> R.id.referrals
        PROFILE -> R.id.profile
    }

    companion object {
        fun fromId(@IdRes id: Int) = when (id) {
            R.id.home -> HOME
            R.id.insurance -> INSURANCE
            R.id.key_gear -> KEY_GEAR
            R.id.referrals -> REFERRALS
            R.id.profile -> PROFILE
            else -> null
        }
    }
}
