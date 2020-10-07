package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.util.extensions.getMarket

abstract class MarketProvider {
    abstract val market: Market?
}

class MarketProviderImpl(
    private val context: Context
) : MarketProvider() {
    override val market = context.getMarket()
}
