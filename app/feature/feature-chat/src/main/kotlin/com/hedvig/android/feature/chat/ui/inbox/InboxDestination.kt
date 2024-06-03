package com.hedvig.android.feature.chat.ui.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.layout.withoutPlacement
import com.hedvig.android.core.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.TripleCase
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.chat.model.Conversation
import com.hedvig.android.feature.chat.model.legacyCheckPoint
import hedvig.resources.R
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate

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
  Column {
    val legacyConversation = conversations.filterIsInstance<Conversation.LegacyConversation>().firstOrNull()
    if (legacyConversation != null) {
      if (legacyConversation.isClosed) {
        ClosedLegacyConversation(
          onConversationClick = { onConversationClick(legacyConversation.conversationId) },
        )
      } else {
        ConversationCard(
          conversation = legacyConversation,
          onConversationClick = { onConversationClick(legacyConversation.conversationId) },
        )
      }
      HorizontalDivider(Modifier.height(1.dp))
    }
    val conversationsInOrder = conversations.filterNot { it is Conversation.LegacyConversation }
      .sortedByDescending { it.lastUpdatedTime }
    LazyColumn {
      items(conversationsInOrder, {
        it.conversationId
      }) { conversation ->
        ConversationCard(conversation = conversation, onConversationClick = onConversationClick)
        HorizontalDivider()
      }
    }
  }
}

@Composable
private fun ClosedLegacyConversation(onConversationClick: () -> Unit, modifier: Modifier = Modifier) {
  val text = "Conversation history until ${legacyCheckPoint.date}" // todo: remove hardcoded string
  Row(
    modifier = modifier
      .padding(top = 12.dp, end = 16.dp, bottom = 12.dp)
      .clickable { onConversationClick() },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      Icons.Hedvig.Document,
      contentDescription = null,
      modifier = Modifier.weight(1f).size(12.dp),
    ) // todo: icon?
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(8f),
    )
  }
}

@Composable
private fun ConversationCard(
  conversation: Conversation,
  onConversationClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateFormatter = rememberHedvigDateTimeFormatter()
  Column(
    modifier = modifier
      .padding(top = 12.dp, bottom = 12.dp, end = 16.dp)
      .clickable { onConversationClick(conversation.conversationId) },
  ) {
    Row(Modifier.height(IntrinsicSize.Min)) {
      val notificationColor = MaterialTheme.colorScheme.error
      val modifierWithDot = Modifier
        .weight(1f)
        .drawWithContent {
          drawContent() // todo: do you need this?
          drawCircle(
            notificationColor,
            radius = 5.dp.toPx(),
            center = this.center,
          )
        }
      val notificationModifier = if (conversation.hasNewMessages) modifierWithDot else Modifier
      val title = when (conversation) {
        is Conversation.ClaimConversation -> conversation.title ?: stringResource(
          R.string.claim_casetype_insurance_case,
        )
        is Conversation.LegacyConversation -> "Conversation history until ${legacyCheckPoint.date}" // todo: remove hardcoded string
        is Conversation.ServiceConversation -> conversation.title ?: "Placeholder service question title" // todo: remove hardcoded string
      }
      Box(
        modifier = notificationModifier
          .weight(1f)
          .fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {}
      Text(
        text = title,
        modifier = Modifier.weight(8f),
        style = MaterialTheme.typography.bodyLarge,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
      )
    }
    Spacer(Modifier.height(4.dp))
    val detailsModifier = Modifier.then(
      if (conversation is Conversation.LegacyConversation) {
        Modifier.withoutPlacement()
      } else {
        Modifier
      },
    )
    Row(modifier = detailsModifier.height(IntrinsicSize.Min)) {
      Box(Modifier.weight(1f))
      Row(Modifier.weight(8f)) {
        val label = when (conversation) {
          is Conversation.ClaimConversation -> conversation.label
          is Conversation.LegacyConversation -> ""
          is Conversation.ServiceConversation -> "Question" // todo: remove hardcode
        }
        val submitted = when (conversation) {
          is Conversation.LegacyConversation -> ""
          is Conversation.ServiceConversation -> "Submitted ${dateFormatter.format(
            conversation.createdAt.date.toJavaLocalDate(),
          )}" // todo: remove hardcode
          is Conversation.ClaimConversation -> "Submitted ${dateFormatter.format(
            conversation.createdAt.date.toJavaLocalDate(),
          )}" // todo: remove hardcode
        }
        Text(
          modifier = Modifier.padding(end = 6.dp),
          text = label,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        )
        VerticalDivider(
          Modifier
            .width(1.dp)
            .fillMaxHeight(),
        )
        Text(
          modifier = Modifier.padding(start = 6.dp),
          text = submitted,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        )
      }
    }
    Spacer(Modifier.height(8.dp))
    Row {
      Box(Modifier.weight(1f))
      Row(Modifier.weight(8f)) {
        Text(
          text = conversation.lastMessageForPreview,
          style = MaterialTheme.typography.bodyMedium,
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
        InboxUiState.Success(listOf(previewServiceConversation, previewClaimConversation, previewLegacyConversation)),
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
          TripleCase.FIRST -> previewLegacyConversation
          TripleCase.SECOND -> previewServiceConversation
          TripleCase.THIRD -> previewClaimConversation
        },
        onConversationClick = {},
      )
    }
  }
}

private val previewServiceConversation = Conversation.ServiceConversation(
  conversationId = "123",
  lastMessageForPreview = "Lorem ipsum dolor sit amet consectetur. Accumsan vitae adipiscing blandit id et interdum Lorem ipsum dolor sit amet consectetur. Accumsan vitae adipiscing blandit id et interdum.",
  hasNewMessages = true,
  lastUpdatedTime = LocalDateTime(2024, 5, 26, 13, 13, 0, 0),
  createdAt = LocalDateTime(2024, 5, 25, 13, 13, 0, 0),
  title = null,
)

private val previewClaimConversation = Conversation.LegacyConversation(
  conversationId = "108",
  lastMessageForPreview = "Lorem ipsum dolor sit amet consectetur. Accumsan vitae adipiscing blandit id et interdum Lorem ipsum dolor sit amet consectetur. Accumsan vitae adipiscing blandit id et interdum.",
  closedAt = LocalDateTime(2024, 3, 26, 13, 13, 0, 0),
  lastUpdatedTime = LocalDateTime(2024, 3, 26, 13, 13, 0, 0),
  hasNewMessages = false,
)

private val previewLegacyConversation = Conversation.ClaimConversation(
  title = "Accident abroad",
  label = "Home",
  conversationId = "2344",
  createdAt = LocalDateTime(2024, 5, 15, 13, 13, 0, 0),
  hasNewMessages = false,
  lastMessageForPreview = "Thanks, please file a report to your BRF and send in the documents and we’ll take a further look.",
  lastUpdatedTime = LocalDateTime(2024, 5, 26, 13, 16, 0, 0),
)
