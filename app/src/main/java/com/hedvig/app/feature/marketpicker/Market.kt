package com.hedvig.app.feature.marketpicker

import com.hedvig.app.R

enum class Market {
    SE,
    NO,
    DK;

    val flag: Int
        get() = when (this) {
            SE -> R.drawable.ic_flag_se
            NO -> R.drawable.ic_flag_no
            DK -> R.drawable.ic_flag_dk
        }

    val label: Int
        get() = when (this) {
            SE -> R.string.sweden
            NO -> R.string.norway
            DK -> R.string.denmark
        }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}
