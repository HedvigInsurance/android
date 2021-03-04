package com.hedvig.app.feature.marketpicker

import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.jsonObjectOf
import com.mixpanel.android.mpmetrics.MixpanelAPI

class MarketPickerTracker(
    private val mixpanel: MixpanelAPI
) {
    fun selectMarket(market: Market) = mixpanel.track(
        "select_market",
        jsonObjectOf(
            "market" to market.toString()
        )
    )

    fun selectLocale(locale: Language) = mixpanel.track(
        "select_locale",
        jsonObjectOf(
            "locale" to locale.toString()
        )
    )

    fun submit() = mixpanel.track("select_market_submit")
}
