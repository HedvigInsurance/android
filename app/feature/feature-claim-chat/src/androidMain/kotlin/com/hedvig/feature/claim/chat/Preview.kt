package com.hedvig.feature.claim.com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.feature.claim.chat.AssistantChatMessage
import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.ConversationScreen
import com.hedvig.feature.claim.chat.UserChatBubble

@Preview(name = "User Bubble", showBackground = true)
@Composable
fun PreviewUserChatBubble() {
  HedvigTheme {
    UserChatBubble(text = "I dropped my AirPods in the pool, they're completely ruined.")
  }
}

@Preview(name = "Assistant Message", showBackground = true)
@Composable
fun PreviewAssistantMessage() {
  HedvigTheme {
    AssistantChatMessage(text = "Hello.", subText = "How can we help you?")
  }
}

@Preview(name = "ChatScreen", showBackground = true)
@Composable
fun PreviewChatScreen() {
  HedvigTheme {
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
