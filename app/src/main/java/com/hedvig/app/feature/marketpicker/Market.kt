package com.hedvig.app.feature.marketpicker

import com.hedvig.app.feature.settings.Language

enum class Market {
    SE {
        val languages = listOf<Language>(Language.EN_SE, Language.SV_SE)
    },
    NO;

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}

data class MarketModel(
    val market: Market,
    val selected: Boolean = false
)
