package com.hedvig.android.feature.chat.navigation

import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
object ChatDestination : Destination

sealed interface ChatDestinations {
  @Serializable
  object Inbox : ChatDestinations, Destination

  @Serializable
  data class Chat(
    val conversationId: String,
  ) : ChatDestinations, Destination
}
