
package com.hedvig.android.feature.chat.inbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.chat.model.InboxConversation
import com.hedvig.android.feature.chat.model.InboxConversation.Header
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.File
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.Text
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.Unknown
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.ui.formattedChatDateTime
import hedvig.resources.R
import kotlinx.datetime.Clock

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
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      TopAppBarWithBack(
        title = stringResource(R.string.CHAT_CONVERSATION_INBOX),
        onClick = navigateUp,
      )
      when (uiState) {
        InboxUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
        InboxUiState.Failure -> HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.weight(1f),
        )

        is InboxUiState.Success -> InboxSuccessScreen(
          uiState.inboxConversations,
          onConversationClick,
        )
      }
    }
  }
}

@Composable
private fun InboxSuccessScreen(inboxConversations: List<InboxConversation>, onConversationClick: (id: String) -> Unit) {
  val lazyListState = rememberLazyListState()
  SideEffect {
    // Keep at the top of the list if we are already at the top and there is a re-arrangement
    // https://slack-chats.kotlinlang.org/t/20209529/ujn92aya0-wave-i-have-a-reversed-lazycolumn-and-its-last-ite#e0eabbdf-ae4b-420a-9bca-2a9de96ed2bd
    if (!lazyListState.canScrollBackward && lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
      lazyListState.requestScrollToItem(0)
    }
  }
  LazyColumn(
    state = lazyListState,
    contentPadding = WindowInsets.safeDrawing
      .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
      .asPaddingValues(),
  ) {
    itemsIndexed(
      items = inboxConversations,
      key = { _, item -> item.conversationId },
    ) { index, conversation ->
      Column(
        modifier = Modifier.animateItem(
          fadeInSpec = null,
          fadeOutSpec = null,
        ),
      ) {
        if (index != 0) {
          HorizontalDivider()
        }
        ConversationCard(
          conversation = conversation,
          onConversationClick = onConversationClick,
        )
      }
    }
  }
}

@Composable
private fun ConversationCard(
  conversation: InboxConversation,
  onConversationClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier = modifier,
    onClick = { onConversationClick(conversation.conversationId) },
    shape = RectangleShape,
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (conversation.hasNewMessages) {
        HedvigTheme.colorScheme.surfacePrimary
      } else {
        HedvigTheme.colorScheme.backgroundPrimary
      },
      contentColor = HedvigTheme.colorScheme.textPrimary,
    ),
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp, bottom = 18.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        {
          Text(
            text = when (conversation.header) {
              Header.Legacy -> stringResource(R.string.CHAT_CONVERSATION_HISTORY_TITLE)
              is Header.ClaimConversation -> stringResource(R.string.home_claim_card_pill_claim)
              Header.ServiceConversation -> stringResource(R.string.CHAT_CONVERSATION_QUESTION_TITLE)
            },
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.wrapContentSize(Alignment.TopStart),
          )
        },
        {
          Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            if (conversation.hasNewMessages) {
              HedvigCard(
                colors = CardDefaults.outlinedCardColors(
                  containerColor = MaterialTheme.colorScheme.infoContainer,
                  contentColor = MaterialTheme.colorScheme.onInfoContainer,
                ),
                shape = MaterialTheme.shapes.squircleExtraSmall,
              ) {
                Text(
                  text = stringResource(R.string.CHAT_NEW_MESSAGE),
                  style = MaterialTheme.typography.labelLarge,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                )
              }
            } else {
              val formattedLastMessageSent = conversation.lastMessageTimestamp.formattedChatDateTime(getLocale())
              Text(
                text = formattedLastMessageSent,
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        },
        spaceBetween = 8.dp,
      )
      val subtitle = when (val header = conversation.header) {
        Header.Legacy -> null
        is Header.ClaimConversation -> header.claimType
        Header.ServiceConversation -> null
      }
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyLarge.copy(color = HedvigTheme.colorScheme.textSecondary),
          modifier = Modifier.wrapContentSize(Alignment.TopStart),
        )
      }
      val latestMessage = conversation.latestMessage
      if (latestMessage != null) {
        Spacer(Modifier.height(8.dp))
        val sender = stringResource(
          when (latestMessage.sender) {
            Sender.HEDVIG -> R.string.CHAT_SENDER_HEDVIG
            Sender.MEMBER -> R.string.CHAT_SENDER_MEMBER
          },
        )
        val message = when (latestMessage) {
          is Text -> latestMessage.text
          is File -> stringResource(R.string.CHAT_SENT_A_FILE)
          is Unknown -> stringResource(R.string.CHAT_SENT_A_MESSAGE)
        }
        Text(
          text = "$sender: $message",
          style = if (conversation.hasNewMessages) {
            MaterialTheme.typography.bodyMedium
          } else {
            MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
        )
      }
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun InboxSuccessScreenPreview() {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    com.hedvig.android.design.system.hedvig.HedvigTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
        InboxScreen(
          InboxUiState.Success(
            listOf(
              mockInboxConversation1,
              mockInboxConversation2,
              mockInboxConversation3,
              mockInboxConversation2.copy(conversationId = "100"),
              mockInboxConversation3.copy(conversationId = "101"),
              mockInboxConversationLegacy,
            ),
          ),
          {},
          {},
          {},
        )
      }
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun ConversationCardPreview(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) cases: TripleCase,
) {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    com.hedvig.android.design.system.hedvig.HedvigTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
        ConversationCard(
          conversation = when (cases) {
            TripleCase.FIRST -> mockInboxConversation1
            TripleCase.SECOND -> mockInboxConversation2
            TripleCase.THIRD -> mockInboxConversationLegacy
          },
          onConversationClick = {},
        )
      }
    }
  }
}

private val mockInboxConversation1 = InboxConversation(
  conversationId = "1",
  header = Header.ClaimConversation("claimType"),
  latestMessage = InboxConversation.LatestMessage.Text(
    "Please tell us more about how the phone broke.",
    Sender.HEDVIG,
    Clock.System.now(),
  ),
  hasNewMessages = true,
  createdAt = Clock.System.now(),
)

private val mockInboxConversation2 = InboxConversation(
  conversationId = "2",
  header = Header.ClaimConversation("claimType"),
  latestMessage = InboxConversation.LatestMessage.File(Sender.MEMBER, Clock.System.now()),
  hasNewMessages = false,
  createdAt = Clock.System.now(),
)

private val mockInboxConversation3 = InboxConversation(
  conversationId = "3",
  header = Header.ServiceConversation,
  latestMessage = InboxConversation.LatestMessage.File(Sender.MEMBER, Clock.System.now()),
  hasNewMessages = false,
  createdAt = Clock.System.now(),
)

private val mockInboxConversationLegacy = InboxConversation(
  conversationId = "999",
  header = Header.Legacy,
  latestMessage = InboxConversation.LatestMessage.File(Sender.MEMBER, Clock.System.now()),
  hasNewMessages = true,
  createdAt = Clock.System.now(),
)
