package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ClaimChatDestination() {
  val claimChatViewModel = ClaimChatViewModel() // todo real VM
  ClaimChatScreen(claimChatViewModel)
}


@Composable
fun ClaimChatScreen(claimChatViewModel: ClaimChatViewModel) {
  Box(Modifier.fillMaxSize(), Alignment.Center) {
    ConversationScreen(
      listOf(
        ConversationItem.AssistantMessage("Hello.", "how can we help you?"),
        ConversationItem.UserMessage("I dropped my AirPods in the pool, they're completely ruined."),
        ConversationItem.AssistantMessage("Ok, I see.", "If you have a receipt please scan in and we can sort this out."),
        ConversationItem.ActionPrompt,
        ConversationItem.AssistantLoadingState("Processing audio file", "Interpreting information..."),
        ConversationItem.CompensationSelection,
      ),
      onAction = {},
    )
  }
}
