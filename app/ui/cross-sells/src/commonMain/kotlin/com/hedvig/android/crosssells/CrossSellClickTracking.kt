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
 * The offer is described by [offerId] as the backend names it (`crossSell:ACCIDENT`) and the campaign code its
 * store URL applies, if any, rather than by the URL itself. Firebase Analytics drops any event parameter value
 * longer than 100 characters, and the store URL is a ~300 character redirect link. The store page the link lands
 * on is left out because the backend derives it from the same offer as [offerId].
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
    URLDecoder.decode(pair.substringAfter('=', ""), "UTF-8").takeIf { it.isNotEmpty() }
  }
}
