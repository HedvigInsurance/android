package com.hedvig.android.feature.chat.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
object ChatDestination : Destination

internal sealed interface ChatDestinations {
  @Serializable
  object Inbox : ChatDestinations, Destination

  @Serializable
  data class Chat(
    val conversationId: String,
  ) : ChatDestinations,
    Destination
}
