package com.hedvig.android.feature.chat.data

import com.benasher44.uuid.Uuid
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.crosssells.CrossSellUserFlow
import com.hedvig.android.crosssells.trackCrossSellClicked
import com.hedvig.android.crosssells.trackCrossSellDismissed
import com.hedvig.android.crosssells.trackCrossSellShown
import com.hedvig.android.feature.chat.data.InChatCrossSellTrackingEvent.CLICKED
import com.hedvig.android.feature.chat.data.InChatCrossSellTrackingEvent.DISMISSED
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * What the in-chat cross-sell card reports and causes, besides leaving the screen: the Firebase cross-sell events,
 * the Datadog funnel action, and ending the offer for the conversation once it is taken or turned down.
 */
internal class InChatCrossSellEvents(
  private val conversationId: Uuid,
  private val eventTrackingClient: EventTrackingClient,
  private val hideInChatCrossSellUseCase: HideInChatCrossSellUseCase,
) {
  fun shown(offerId: String, offerUrl: String) {
    eventTrackingClient.trackCrossSellShown(CrossSellUserFlow.IN_CHAT_X_SELL, offerId, offerUrl)
  }

  suspend fun dismissed(offerId: String, offerUrl: String) {
    eventTrackingClient.trackCrossSellDismissed(CrossSellUserFlow.IN_CHAT_X_SELL, offerId, offerUrl)
    logInChatCrossSell(DISMISSED)
    endOffer()
  }

  /** Taking the offer ends it for this conversation, just as turning it down does. */
  suspend fun clicked(offerId: String, offerUrl: String) {
    eventTrackingClient.trackCrossSellClicked(CrossSellUserFlow.IN_CHAT_X_SELL, offerId, offerUrl)
    logInChatCrossSell(CLICKED)
    endOffer()
  }

  private suspend fun endOffer() {
    hideInChatCrossSellUseCase.invoke(conversationId).onLeft {
      logcat(LogPriority.WARN) { "Could not hide the in-chat cross sell for $conversationId: ${it.message}" }
    }
  }
}
