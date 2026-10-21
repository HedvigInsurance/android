package com.hedvig.android.crosssells

import com.hedvig.android.core.tracking.EventTrackingClient
import java.net.URLDecoder

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
 * The offer is described by [offerId] as the backend names it (`crossSell:ACCIDENT`) and by the campaign code its
 * store URL applies, if any. Both stay well under Firebase Analytics' 100 character limit for event parameter
 * values, above which a value is dropped.
 */
fun EventTrackingClient.trackCrossSellClicked(userFlow: CrossSellUserFlow, offerId: String, offerUrl: String) {
  trackEvent(
    name = "cross_sell_clicked",
    parameters = buildMap {
      put("user_flow", userFlow.analyticsValue)
      put("offer_id", offerId)
      campaignCode(offerUrl)?.let { put("campaign_code", it) }
    },
  )
}

internal fun campaignCode(offerUrl: String): String? = queryParameter(offerUrl, "code")

private fun queryParameter(url: String, name: String): String? {
  val query = url.substringAfter('?', "").substringBefore('#')
  if (query.isEmpty()) return null
  return query.split('&').firstNotNullOfOrNull { pair ->
    if (pair.substringBefore('=') != name) return@firstNotNullOfOrNull null
    val value = pair.substringAfter('=', "")
    // A malformed escape must not crash the click that opens the store.
    val decoded = try {
      URLDecoder.decode(value, "UTF-8")
    } catch (_: IllegalArgumentException) {
      value
    }
    decoded.takeIf { it.isNotEmpty() }
  }
}
