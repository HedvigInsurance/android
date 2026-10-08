package com.hedvig.android.feature.chat.data

import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.core.tracking.logAction

/**
 * Datadog RUM carries the in-chat cross-sell funnel: how many members the card reached, how many
 * turned it down and how many went on to the offer. Firebase gets the same funnel under the
 * tracking plan's cross-sell event names, so it sits next to the other cross-sell surfaces.
 */
internal fun logInChatCrossSell(event: InChatCrossSellTrackingEvent, eventTrackingClient: EventTrackingClient) {
  logAction(
    type = ActionType.CUSTOM,
    name = "inChatCrossSell",
    attributes = mapOf(
      "event" to event.attributeValue,
    ),
  )
  eventTrackingClient.trackEvent(
    name = event.analyticsEventName,
    parameters = mapOf(
      "user_flow" to "in_chat_x_sell",
      // The chat only ever recommends a new insurance, never an add-on
      "offer_type" to "new_promise",
    ),
  )
}

internal enum class InChatCrossSellTrackingEvent(val attributeValue: String, val analyticsEventName: String) {
  PROMPTED("prompted", "cross_sell_shown"),
  DISMISSED("dismissed", "cross_sell_dismissed"),
  CLICKED("clicked", "cross_sell_clicked"),
}
