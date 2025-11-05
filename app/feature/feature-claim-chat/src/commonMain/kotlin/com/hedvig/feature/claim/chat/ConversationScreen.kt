package com.hedvig.feature.claim.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

sealed class ConversationItem {
  data class UserMessage(val text: String) : ConversationItem()
  data class AssistantMessage(val text: String, val subText: String) : ConversationItem()
  data class AssistantLoadingState(val text: String, val subText: String) : ConversationItem()
  object ActionPrompt : ConversationItem()
  object CompensationSelection : ConversationItem()
}

@Composable
fun ConversationScreen(
  conversation: List<ConversationItem>,
  onAction: (String) -> Unit,
) {

  Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F4F9))) {

    Column(modifier = Modifier.fillMaxSize()) {
      LazyColumn(
        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(conversation) { item ->
          when (item) {
            is ConversationItem.UserMessage ->
              UserChatBubble(item.text)

            is ConversationItem.AssistantMessage ->
              AssistantChatMessage(item.text, item.subText)

            is ConversationItem.AssistantLoadingState ->
              AssistantLoadingState(item.text, item.subText)

            ConversationItem.ActionPrompt ->
              BottomActionButtons(
                onDontHaveReceipt = { onAction("dont_have_receipt") },
                onScanReceipt = { onAction("scan_receipt") },
              )
            ConversationItem.CompensationSelection -> TODO()
          }
        }
      }
    }
  }
}
