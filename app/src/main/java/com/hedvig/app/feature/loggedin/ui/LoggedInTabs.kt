package com.hedvig.app.feature.loggedin.ui

import android.content.res.Resources
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

    fun backgroundGradient(resources: Resources) = resources.getIntArray(
        when (this) {
            HOME -> R.array.home_gradient
            INSURANCE -> R.array.insurance_gradient
            KEY_GEAR -> R.array.key_gear_gradient
            REFERRALS -> R.array.forever_gradient
            PROFILE -> R.array.profile_gradient
        }
    )

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
