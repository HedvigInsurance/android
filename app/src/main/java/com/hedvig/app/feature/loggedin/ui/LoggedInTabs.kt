package com.hedvig.app.feature.loggedin.ui

import androidx.annotation.IdRes
import com.hedvig.app.R

enum class LoggedInTabs {
    DASHBOARD,
    CLAIMS,
    KEY_GEAR,
    REFERRALS,
    PROFILE;

    @IdRes
    fun id() = when (this) {
        DASHBOARD -> R.id.dashboard
        CLAIMS -> R.id.claims
        KEY_GEAR -> R.id.key_gear
        REFERRALS -> R.id.referrals
        PROFILE -> R.id.profile
    }

    companion object {
        fun fromId(@IdRes id: Int) = when (id) {
            R.id.dashboard -> DASHBOARD
            R.id.claims -> CLAIMS
            R.id.key_gear -> KEY_GEAR
            R.id.referrals -> REFERRALS
            R.id.profile -> PROFILE
            else -> null
        }
    }
}
