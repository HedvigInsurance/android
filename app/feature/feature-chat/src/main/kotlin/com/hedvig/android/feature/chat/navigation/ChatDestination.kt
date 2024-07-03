package com.hedvig.android.feature.chat.navigation

import com.benasher44.uuid.Uuid
import com.kiwi.navigationcompose.typed.Destination

object ChatDestination : Destination

internal sealed interface ChatDestinations {
  object Inbox : Destination

  data class Chat(
    val conversationId: Uuid,
  ) : Destination
}
