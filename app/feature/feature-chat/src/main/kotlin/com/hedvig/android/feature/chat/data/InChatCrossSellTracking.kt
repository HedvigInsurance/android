package com.hedvig.android.feature.chat.data

import com.benasher44.uuid.Uuid
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction

/**
 * Datadog RUM carries the in-chat cross-sell funnel: how many members the card reached, how many
 * turned it down and how many went on to the offer.
 */
internal fun logInChatCrossSell(event: InChatCrossSellTrackingEvent, conversationId: Uuid, crossSellId: String) {
  logAction(
    type = ActionType.CUSTOM,
    name = "inChatCrossSell",
    attributes = mapOf(
      "event" to event.attributeValue,
      "conversationId" to conversationId.toString(),
      "crossSellId" to crossSellId,
    ),
  )
}

internal enum class InChatCrossSellTrackingEvent(val attributeValue: String) {
  PROMPTED("prompted"),
  DISMISSED("dismissed"),
  CLICKED("clicked"),
}
