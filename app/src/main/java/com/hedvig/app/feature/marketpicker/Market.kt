package com.hedvig.app.feature.marketpicker

import com.hedvig.app.R

enum class Market {
    SE,
    NO;

    fun getFlag() = when (this) {
        SE -> R.drawable.ic_flag_se
        NO -> R.drawable.ic_flag_no
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}
