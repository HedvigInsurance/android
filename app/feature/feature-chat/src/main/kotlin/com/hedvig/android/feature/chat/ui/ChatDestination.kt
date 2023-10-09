package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.normal.ChevronUp
import com.hedvig.android.feature.chat.ChatEventNew
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.ChatViewModelNew
import com.hedvig.android.feature.chat.data.ChatMessage
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

@Composable
internal fun ChatDestination(
  viewModel: ChatViewModelNew,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChatScreen(
    uiState = uiState,
    onSendMessage = { message: String ->
      viewModel.emit(ChatEventNew.SendTextMessage(message))
    },
    onSendFile = { file: File ->
      viewModel.emit(ChatEventNew.SendFileMessage(file))
    },
    onFetchMessages = { until: Instant ->
      viewModel.emit(ChatEventNew.FetchMessages(until))
    },
    onDismissError = {
      viewModel.emit(ChatEventNew.DismissError)
    },
  )
}

@Composable
internal fun ChatScreen(
  uiState: ChatUiState,
  onSendMessage: (String) -> Unit,
  onSendFile: (File) -> Unit,
  onFetchMessages: (Instant) -> Unit,
  onDismissError: () -> Unit,
) {
  var message by remember { mutableStateOf("") }

  Box(
    Modifier
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .fillMaxHeight(),
  ) {
    Column(
      Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxWidth()
        .padding(bottom = 80.dp),
    ) {
      uiState.messages.forEach { message ->
        Row(
          modifier = Modifier.align(message.getMessageAlignment()),
        ) {
          MessageView(chatMessage = message)
        }
      }
    }

    Row(
      modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
      verticalAlignment = Alignment.Bottom,
    ) {
      HedvigTextField(
        value = message,
        onValueChange = {
          message = it
        },
        errorText = null,
        trailingIcon = {
          IconButton(
            onClick = {
              onSendMessage(message)
              message = ""
            },
            modifier = Modifier.size(30.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.ChevronUp,
              contentDescription = null,
            )
          }
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun MessageView(
  chatMessage: ChatMessage,
) {
  when (chatMessage) {
    is ChatMessage.ChatMessageFile -> Text("file")
    is ChatMessage.ChatMessageText -> TextMessage(chatMessage)
  }
}

@Composable
private fun TextMessage(
  chatMessage: ChatMessage.ChatMessageText,
) {
  Column(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
  ) {
    Surface(
      shape = MaterialTheme.shapes.squircleMedium,
      color = chatMessage.getColor(),
      modifier = Modifier.align(chatMessage.getMessageAlignment()),
    ) {
      Text(
        text = chatMessage.text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = chatMessage.formattedDateTime(),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(chatMessage.getMessageAlignment())
        .padding(horizontal = 4.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun ChatScreenPreview(
  @PreviewParameter(ChatUiStateProvider::class) chatUiState: ChatUiState,
) {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    ) {
      ChatScreen(
        uiState = chatUiState,
        onSendMessage = {},
        onSendFile = {},
        onFetchMessages = {},
        onDismissError = {},
      )
    }
  }
}

private class ChatUiStateProvider : CollectionPreviewParameterProvider<ChatUiState>(
  listOf(
    ChatUiState(
      isLoadingChat = false,
      isLoadingNewMessages = false,
      isSendingMessage = false,
      messages = persistentListOf(
        ChatMessage.ChatMessageText(
          id = "1",
          sender = ChatMessage.Sender.MEMBER,
          sentAt = LocalDateTime.now().plusDays(4).toInstant(ZoneOffset.UTC).toKotlinInstant(),
          text = "Hello",
        ),
        ChatMessage.ChatMessageText(
          id = "2",
          sender = ChatMessage.Sender.HEDVIG,
          sentAt = LocalDateTime.now().plusDays(3).toInstant(ZoneOffset.UTC).toKotlinInstant(),
          text = "Hello from Hedvig!",
        ),
        ChatMessage.ChatMessageText(
          id = "3",
          sender = ChatMessage.Sender.HEDVIG,
          sentAt = LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.UTC).toKotlinInstant(),
          text = "Are you there?",
        ),
        ChatMessage.ChatMessageText(
          id = "1",
          sender = ChatMessage.Sender.MEMBER,
          sentAt = LocalDateTime.now().toInstant(ZoneOffset.UTC).toKotlinInstant(),
          text = "Yes I have a claim that I need help with. Could you please direct me to the correct flow in the app please?    Thank you",
        ),
      ),
      errorMessage = "TestError",
    ),
  ),
)
