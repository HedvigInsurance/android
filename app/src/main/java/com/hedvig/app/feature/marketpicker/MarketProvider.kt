package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.HedvigApplication
import com.hedvig.app.shouldOverrideFeatureFlags
import com.hedvig.app.util.extensions.getMarket

abstract class MarketProvider {
    abstract val market: Market?
    abstract val enabledMarkets: List<Market>
}

class MarketProviderImpl(
    private val context: Context,
    app: HedvigApplication
) : MarketProvider() {
    override val market
        get() = context.getMarket()

    override val enabledMarkets = listOfNotNull(
        Market.SE,
        Market.NO,
        if (shouldOverrideFeatureFlags(app)) {
            Market.DK
        } else {
            null
        }
    )
}
