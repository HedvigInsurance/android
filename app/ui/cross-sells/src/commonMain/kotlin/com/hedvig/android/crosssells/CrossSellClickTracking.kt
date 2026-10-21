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
 * The offer is described by three short values rather than its store URL. Firebase Analytics drops any event
 * parameter value longer than 100 characters (the whole parameter, not just the excess), and the store URL is a
 * ~300 character redirect link whose first 100 characters are the same fixed prefix on every offer. So the event
 * carries the parts that identify the offer: [offerId] as the backend names it (`crossSell:ACCIDENT`), the store
 * page the link lands on, and the campaign code the link applies, if any. All three are read off the backend's
 * response; the app attaches no meaning to them.
 */
fun EventTrackingClient.trackCrossSellClicked(userFlow: CrossSellUserFlow, offerId: String, offerUrl: String) {
  trackEvent(
    name = "cross_sell_clicked",
    parameters = buildMap {
      put("user_flow", userFlow.analyticsValue)
      // A cross-sell is always a new insurance. Add-ons are bought in-app through their own flow.
      put("offer_type", "new_promise")
      put("offer_id", offerId)
      offerPath(offerUrl)?.let { put("offer_path", it) }
      campaignCode(offerUrl)?.let { put("campaign_code", it) }
    },
  )
}

/** The store page the link lands on: the redirect's `next` parameter, or the link's own path when there is none. */
internal fun offerPath(offerUrl: String): String? {
  queryParameter(offerUrl, "next")?.let { return it }
  val path = offerUrl.substringAfter("://", "").substringBefore('?').substringBefore('#').substringAfter('/', "")
  return if (path.isEmpty()) null else "/$path"
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
