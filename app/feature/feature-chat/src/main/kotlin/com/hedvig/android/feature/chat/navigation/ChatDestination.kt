package com.hedvig.android.feature.chat.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
object ChatDestination : HedvigNavKey

sealed interface ChatDestinations {
  @Serializable
  data class Chat(
    val conversationId: String,
  ) : ChatDestinations, HedvigNavKey
}
