package com.hedvig.android.crosssells

import com.hedvig.android.core.tracking.EventTrackingClient

/** The surface a cross-sell was offered on, named after the backend `UserFlow` the offer was requested with. */
enum class CrossSellUserFlow(val analyticsValue: String) {
  HOME_X_SELL("home_x_sell"),
  HOME("home"),
  INSURANCES("insurances"),
  SMART_X_SELL("smart_x_sell"),
  ONBOARDING("onboarding"),
  IN_CHAT_X_SELL("in_chat_x_sell"),
}

/**
 * Fires `cross_sell_clicked` when a member takes a cross-sell offer and the app sends them to the web store.
 *
 * [offerUrl] is the store URL exactly as the backend returned it, before the member's authorization code is attached,
 * so the funnel can be sliced on whatever the backend put in it (product, discount, campaign) without the app
 * having to know what any of it means. Firebase caps string parameter values at 100 characters.
 */
fun EventTrackingClient.trackCrossSellClicked(userFlow: CrossSellUserFlow, offerUrl: String) {
  trackEvent(
    name = "cross_sell_clicked",
    parameters = mapOf(
      "user_flow" to userFlow.analyticsValue,
      // A cross-sell is always a new insurance. Add-ons are bought in-app through their own flow.
      "offer_type" to "new_promise",
      "offer_url" to offerUrl,
    ),
  )
}
