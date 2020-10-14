package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.feature.adyen.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity

enum class Market {
    SE,
    NO;

    fun connectPayin(context: Context) = when (this) {
        SE -> TrustlyConnectPayinActivity.newInstance(
            context
        )
        NO -> AdyenConnectPayinActivity.newInstance(
            context,
            AdyenCurrency.fromMarket(this)
        )
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}

data class MarketModel(
    val market: Market,
    val selected: Boolean = false
)
