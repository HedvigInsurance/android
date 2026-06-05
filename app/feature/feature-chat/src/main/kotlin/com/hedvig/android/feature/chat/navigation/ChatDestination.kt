package com.hedvig.android.feature.chat.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import kotlinx.serialization.Serializable

@Serializable
data object InboxKey : HedvigNavKey, SuppressesChatPushNotification

@Serializable
data class ChatKey(
  val conversationId: String,
) : HedvigNavKey, SuppressesChatPushNotification
