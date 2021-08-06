package com.hedvig.app.feature.marketpicker

import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class MarketPickerTracker(
    private val trackingFacade: TrackingFacade
) {
    fun selectMarket(market: Market) = trackingFacade.track(
        "select_market",
        jsonObjectOf(
            "market" to market.toString()
        )
    )

    fun selectLocale(locale: Language) = trackingFacade.track(
        "select_locale",
        jsonObjectOf(
            "locale" to locale.toString()
        )
    )

    fun submit() = trackingFacade.track("select_market_submit")
}
