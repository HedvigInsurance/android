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
    percentDecode(pair.substringAfter('=', "")).takeIf { it.isNotEmpty() }
  }
}

/**
 * Decodes `%XX` escapes as UTF-8 and leaves every other character as written, a `+` and a malformed escape
 * included, so it can never fail on a store link the backend built.
 */
internal fun percentDecode(value: String): String {
  if ('%' !in value) return value
  val decoded = StringBuilder(value.length)
  val escapedBytes = ArrayList<Byte>()

  fun flushEscapedBytes() {
    if (escapedBytes.isEmpty()) return
    decoded.append(escapedBytes.toByteArray().decodeToString())
    escapedBytes.clear()
  }
  var index = 0
  while (index < value.length) {
    val high = if (value[index] == '%') value.getOrNull(index + 1)?.digitToIntOrNull(16) else null
    val low = if (high != null) value.getOrNull(index + 2)?.digitToIntOrNull(16) else null
    if (high != null && low != null) {
      escapedBytes.add((high * 16 + low).toByte())
      index += 3
    } else {
      flushEscapedBytes()
      decoded.append(value[index])
      index++
    }
  }
  flushEscapedBytes()
  return decoded.toString()
}
