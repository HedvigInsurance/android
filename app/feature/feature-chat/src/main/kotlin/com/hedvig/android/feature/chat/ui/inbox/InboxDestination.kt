package com.hedvig.android.feature.chat.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.lightTypeContainer
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.HedvigDateTimeFormatterDefaults
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.TripleCase
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.feature.chat.model.Conversation
import com.hedvig.android.feature.chat.ui.formattedDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun InboxDestination(
  viewModel: InboxViewModel,
  navigateUp: () -> Unit,
  onConversationClick: (id: String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InboxScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onConversationClick = onConversationClick,
    reload = { viewModel.emit(InboxEvent.Reload) },
  )
}

@Composable
private fun InboxScreen(
  uiState: InboxUiState,
  navigateUp: () -> Unit,
  onConversationClick: (id: String) -> Unit,
  reload: () -> Unit,
) {
  Column(
    Modifier
      .fillMaxSize()
      .consumeWindowInsets(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
      ),
  ) {
    TopAppBarWithBack(
      title = "Inbox", // todo: remove hardcode
      onClick = navigateUp,
    )
    when (uiState) {
      InboxUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      InboxUiState.Failure -> HedvigErrorSection(
        onButtonClick = reload,
        modifier = Modifier.weight(1f),
      )

      is InboxUiState.Success -> InboxSuccessScreen(
        uiState.conversations,
        onConversationClick,
      )
    }
  }
}

@Composable
private fun InboxSuccessScreen(conversations: List<Conversation>, onConversationClick: (id: String) -> Unit) {
  val conversationsInOrder = conversations.sortedByDescending { it.newestMessageForPreview.sentAt }
  LazyColumn {
    items(
      conversationsInOrder,
      {
        it.conversationId
      },
    ) { conversation ->
      ConversationCard(
        conversation = conversation,
        onConversationClick = onConversationClick,
      )
      HorizontalDivider()
    }
  }
}

@Composable
private fun ConversationCard(
  conversation: Conversation,
  onConversationClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val title = conversation.title
  val subTitle = conversation.subtitle
  val dateOfLatest = if (conversation.isLegacy) {
    "Until ${
      HedvigDateTimeFormatterDefaults.isoLocalDateWithDots(getLocale()).format(
        conversation.newestMessageForPreview.sentAt.toLocalDateTime(
          TimeZone.currentSystemDefault(),
        ).toJavaLocalDateTime(),
      )
    }"
  } else {
    conversation.newestMessageForPreview.formattedDateTime(getLocale()) // todo: copy!!!
  }
  Column(
    modifier = modifier
      .clickable { onConversationClick(conversation.conversationId) }
      .background(
        color = if (conversation.hasNewMessages) {
          MaterialTheme.colorScheme.lightTypeContainer
        } else {
          Color.Transparent
        },
      ),
  ) {
    Row(
      Modifier
        .height(IntrinsicSize.Min)
        .padding(top = 12.dp, end = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val notificationColor = MaterialTheme.colorScheme.error
      val modifierWithDot = Modifier
        .weight(1f)
        .drawWithContent {
          drawCircle(
            notificationColor,
            radius = 5.dp.toPx(),
            center = this.center,
          )
        }
      val notificationModifier = if (conversation.hasNewMessages) modifierWithDot else Modifier
      Box(
        modifier = notificationModifier
          .weight(1f)
          .fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {}
      Text(
        text = title,
        modifier = Modifier.weight(5f),
        style = MaterialTheme.typography.bodyLarge,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
      )
      Text(
        modifier = Modifier.weight(3f),
        text = dateOfLatest,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        textAlign = TextAlign.End,
      )
    }
    if (!conversation.isLegacy) {
      Spacer(Modifier.height(4.dp))
      Row(
        modifier = Modifier
          .height(IntrinsicSize.Min)
          .padding(end = 16.dp),
      ) {
        Box(Modifier.weight(1f))
        Row(Modifier.weight(8f)) {
          Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Row {
      Box(Modifier.weight(1f))
      Row(
        Modifier
          .weight(8f)
          .padding(bottom = 12.dp, end = 16.dp),
      ) {
        val sender = when (conversation.newestMessageForPreview.sender) {
          ChatMessage.Sender.HEDVIG -> "Hedvig"
          ChatMessage.Sender.MEMBER -> "You" // todo: add copy!
        }
        val msgText = when (conversation.newestMessageForPreview) {
          is ChatMessage.ChatMessageFile -> "Sent a file" // todo: add copy!
          is ChatMessage.ChatMessageGif -> "GIF" // todo: add copy!
          is ChatMessage.ChatMessageText -> conversation.newestMessageForPreview.text
          is ChatMessage.FailedToBeSent.ChatMessageText -> conversation.newestMessageForPreview.text
          is ChatMessage.FailedToBeSent.ChatMessageUri -> "Sent a file" // todo: add copy!
        }
        val textForPreview = if (conversation.isLegacy) {
          "Your conversation history from previous contacts with Hedvig while everything was one long chat." // todo: copy!
        } else {
          "$sender: $msgText"
        }
        Text(
          text = textForPreview,
          style = if (conversation.hasNewMessages) {
            MaterialTheme.typography.bodyMedium
          } else {
            MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          overflow = TextOverflow.Ellipsis,
          maxLines = 2,
        )
      }
    }
  }
}

@Preview
@Composable
private fun InboxSuccessScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InboxScreen(
        InboxUiState.Success(listOf(mockConversation1, mockConversation2, mockConversationLegacy)),
        {},
        {},
        {},
      )
    }
  }
}

@Preview
@Composable
private fun ConversationCardPreview(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) cases: TripleCase,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ConversationCard(
        conversation = when (cases) {
          TripleCase.FIRST -> mockConversation1
          TripleCase.SECOND -> mockConversation2
          TripleCase.THIRD -> mockConversationLegacy
        },
        onConversationClick = {},
      )
    }
  }
}

private val mockConversation1 = Conversation(
  conversationId = "1",
  newestMessageForPreview = ChatMessage.ChatMessageText(
    "11",
    ChatMessage.Sender.HEDVIG,
    sentAt = Clock.System.now(),
    text = "Please tell as more about how the phone broke.",
  ),
  hasNewMessages = true,
  chatMessages = listOf(
    ChatMessage.ChatMessageText(
      "11",
      ChatMessage.Sender.HEDVIG,
      sentAt = Clock.System.now(),
      text = "Please tell as more about how the phone broke.",
    ),
  ),
  title = "Claim",
  subtitle = "Broken phone",
  statusMessage = null,
  isLegacy = false,
)

private val mockConversation2 = Conversation(
  conversationId = "2",
  newestMessageForPreview = ChatMessage.ChatMessageFile(
    "Id",
    ChatMessage.Sender.MEMBER,
    mimeType = ChatMessage.ChatMessageFile.MimeType.IMAGE,
    sentAt = Clock.System.now(),
    url = "url",
  ),
  hasNewMessages = false,
  chatMessages = listOf(
    ChatMessage.ChatMessageFile(
      "Id",
      ChatMessage.Sender.MEMBER,
      mimeType = ChatMessage.ChatMessageFile.MimeType.IMAGE,
      sentAt = Clock.System.now(),
      url = "url",
    ),
  ),
  title = "Question",
  subtitle = "Termination",
  statusMessage = null,
  isLegacy = false,
)

private val mockConversationLegacy = Conversation(
  conversationId = "0",
  newestMessageForPreview = ChatMessage.ChatMessageText(
    "11",
    ChatMessage.Sender.HEDVIG,
    sentAt = Instant.fromEpochSeconds(50, 1),
    text = "Please tell as more about how the phone broke.",
  ),
  hasNewMessages = false,
  chatMessages = listOf(
    ChatMessage.ChatMessageText(
      "11",
      ChatMessage.Sender.HEDVIG,
      sentAt = Instant.fromEpochSeconds(50, 1),
      text = "Please tell as more about how the phone broke.",
    ),
  ),
  title = "Conversation history",
  subtitle = "",
  statusMessage = null,
  isLegacy = true,
)
