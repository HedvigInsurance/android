package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.util.extensions.getMarket

class MarketProvider(
    private val context: Context
) {
    val market: Market?
        get() = context.getMarket()
}
