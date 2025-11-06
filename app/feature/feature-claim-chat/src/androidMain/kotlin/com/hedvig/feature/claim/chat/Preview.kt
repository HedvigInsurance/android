package com.hedvig.feature.claim.com.hedvig.feature.claim.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.feature.claim.chat.ConversationItem
import com.hedvig.feature.claim.chat.ConversationScreen
import com.hedvig.feature.claim.chat.ConversationUiState
import com.hedvig.feature.claim.chat.FormField
import com.hedvig.feature.claim.chat.FormFieldType
import com.hedvig.feature.claim.chat.assistantmessage.AssistantChatMessage
import com.hedvig.feature.claim.chat.assistantmessage.UserChatBubble
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState

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
      ConversationUiState(
        listOf(
          ConversationItem.AssistantMessage("Tell us what happened.", "Please be as detailed as possible."),
          ConversationItem.AudioRecording(
            AudioRecorderUiState.AudioRecording.Playback(
              filePath = "",
              isPlaying = false,
              isPrepared = true,
              amplitudes = listOf(),
              hasNextStep = true,
              isLoading = false,
              hasError = false,
            ),
          ),
          ConversationItem.UserMessage("I dropped my AirPods in the pool, they're completely ruined."),
          ConversationItem.AssistantMessage(
            "Ok, I see.",
            "If you have a receipt please scan in and we can sort this out.",
          ),
          ConversationItem.Form(
            listOf(
              FormField(
                id = "1",
                title = "Did you lock your bike?",
                type = FormFieldType.BINARY,
                options = listOf("Yes", "No"),
              ),
            ),
          ),
          ConversationItem.AssistantLoadingState("Processing", "Interpreting information...", true),
          ConversationItem.Form(
            listOf(
              FormField(
                id = "2",
                title = "Purchase price",
                defaultValue = "1000",
                type = FormFieldType.NUMBER,
                suffix = "kr",
              ),
              FormField(
                id = "3",
                title = "Brand",
                type = FormFieldType.SINGLE_SELECT,
                options = listOf("Iphone", "Android", "Oppo"),
              ),
              FormField(
                id = "4",
                title = "Model",
                defaultValue = "Pixel 10",
                type = FormFieldType.TEXT,
              ),
              FormField(
                id = "5",
                title = "Date of occurrence",
                defaultValue = "November 1 2025",
                type = FormFieldType.DATE,
                options = listOf("Iphone", "Android", "Oppo"),
              ),
            ),
          ),
        ),
      ),
      onAction = {},
    )
  }
}
