package com.hedvig.app.feature.marketpicker

enum class Market {
    SE,
    NO;

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}

/*data class MarketModel(
    val market: Market,
    val selected: Boolean = false
)*/
