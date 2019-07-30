package com.hedvig.app.feature.loggedin.ui

import androidx.annotation.IdRes
import com.hedvig.app.R

enum class LoggedInTabs {
    DASHBOARD,
    CLAIMS,
    REFERRALS,
    PROFILE;

    companion object {
        fun fromId(@IdRes id: Int) = when (id) {
            R.id.dashboard -> DASHBOARD
            R.id.claims -> CLAIMS
            R.id.referrals -> REFERRALS
            R.id.profile -> PROFILE
            else -> throw Error("Invalid Menu ID")
        }
    }
}
