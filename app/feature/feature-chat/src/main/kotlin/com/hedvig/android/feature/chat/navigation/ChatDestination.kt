package com.hedvig.android.feature.chat.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object InboxKey : HedvigNavKey

@Serializable
data class ChatKey(
  val conversationId: String,
) : HedvigNavKey
