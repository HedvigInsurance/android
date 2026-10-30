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

/** Fires `cross_sell_shown` when a cross-sell offer has been on screen, once per offer per visit to its surface. */
fun EventTrackingClient.trackCrossSellShown(userFlow: CrossSellUserFlow, offerId: String, offerUrl: String) {
  trackCrossSellEvent("cross_sell_shown", userFlow, offerId, offerUrl)
}

/** Fires `cross_sell_clicked` when a member takes a cross-sell offer and the app sends them to the web store. */
fun EventTrackingClient.trackCrossSellClicked(userFlow: CrossSellUserFlow, offerId: String, offerUrl: String) {
  trackCrossSellEvent("cross_sell_clicked", userFlow, offerId, offerUrl)
}

/** Fires `cross_sell_dismissed` when a member turns a cross-sell offer down. */
fun EventTrackingClient.trackCrossSellDismissed(userFlow: CrossSellUserFlow, offerId: String, offerUrl: String) {
  trackCrossSellEvent("cross_sell_dismissed", userFlow, offerId, offerUrl)
}

/**
 * The offer is described by [offerId] as the backend names it (`crossSell:ACCIDENT`), by the campaign code its
 * store URL applies, if any, and by the experiment groups the backend made it for. The store URL lists those as
 * `attribution.experiments.ACCIDENT_DISCOUNT=NO_DISCOUNT`, and each becomes a parameter named after the experiment.
 * All stay well under Firebase Analytics' 100 character limit for event parameter values, above which a value is
 * dropped.
 */
private fun EventTrackingClient.trackCrossSellEvent(
  name: String,
  userFlow: CrossSellUserFlow,
  offerId: String,
  offerUrl: String,
) {
  trackEvent(
    name = name,
    parameters = buildMap {
      put("user_flow", userFlow.analyticsValue)
      put("offer_id", offerId)
      campaignCode(offerUrl)?.let { put("campaign_code", it) }
      for ((experiment, variant) in experiments(offerUrl)) {
        if (experiment !in this) put(experiment, variant)
      }
    },
  )
}

internal fun campaignCode(offerUrl: String): String? = queryParameters(offerUrl)["code"]

/** The experiment groups in [offerUrl], keyed by experiment, leaving out names Firebase would reject as parameters. */
internal fun experiments(offerUrl: String): Map<String, String> {
  return queryParameters(offerUrl)
    .mapNotNull { (name, value) ->
      val experiment = name.removePrefix(EXPERIMENT_PARAMETER_PREFIX)
      if (experiment == name || !experiment.matches(FIREBASE_PARAMETER_NAME)) return@mapNotNull null
      experiment to value
    }
    .toMap()
}

/** Each named parameter's first non-empty value, decoded. */
private fun queryParameters(url: String): Map<String, String> {
  val query = url.substringAfter('?', "").substringBefore('#')
  if (query.isEmpty()) return emptyMap()
  return buildMap {
    for (pair in query.split('&')) {
      val name = percentDecode(pair.substringBefore('='))
      val value = percentDecode(pair.substringAfter('=', ""))
      if (value.isNotEmpty() && name !in this) put(name, value)
    }
  }
}

private const val EXPERIMENT_PARAMETER_PREFIX = "attribution.experiments."
private val FIREBASE_PARAMETER_NAME = Regex("[A-Za-z][A-Za-z0-9_]{0,39}")

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
