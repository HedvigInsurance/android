package com.hedvig.android.feature.chat.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.StartClaimBottomSheet
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.datepicker.formatInstantForTalkBack
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.PenEdit
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.chat.model.InboxConversation
import com.hedvig.android.feature.chat.model.InboxConversation.Header
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.File
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.Text
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage.Unknown
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.ui.formattedChatDateTime
import hedvig.resources.CHAT_CONVERSATION_HISTORY_TITLE
import hedvig.resources.CHAT_CONVERSATION_INBOX
import hedvig.resources.CHAT_CONVERSATION_QUESTION_TITLE
import hedvig.resources.CHAT_NEW_MESSAGE
import hedvig.resources.CHAT_SENDER_MEMBER
import hedvig.resources.CHAT_SENT_A_FILE
import hedvig.resources.CHAT_SENT_A_MESSAGE
import hedvig.resources.HC_CHAT_BUTTON
import hedvig.resources.HEDVIG_NAME_TEXT
import hedvig.resources.INBOX_EMPTY_STATE_SUBTITLE
import hedvig.resources.INBOX_EMPTY_STATE_TITLE
import hedvig.resources.INBOX_NEW_MESSAGE
import hedvig.resources.INBOX_NEW_MESSAGE_CLAIM_DESCRIPTION
import hedvig.resources.INBOX_NEW_MESSAGE_SUPPORT_DESCRIPTION
import hedvig.resources.Res
import hedvig.resources.TALKBACK_CONVERSATION_DESCRIPTION
import hedvig.resources.TERMINATION_FLOW_TODAY
import hedvig.resources.claim_status_bar_closed
import hedvig.resources.general_close_button
import hedvig.resources.home_claim_card_pill_claim
import hedvig.resources.home_tab_claim_button_text
import hedvig.resources.open_chat
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InboxDestination(
  viewModel: InboxViewModel,
  navigateUp: () -> Unit,
  onConversationClick: (id: String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimChat: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InboxScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onConversationClick = onConversationClick,
    reload = { viewModel.emit(InboxEvent.Reload) },
    onNavigateToNewConversation = onNavigateToNewConversation,
    navigateToClaimChat = navigateToClaimChat,
  )
}

@Composable
private fun InboxScreen(
  uiState: InboxUiState,
  navigateUp: () -> Unit,
  onConversationClick: (id: String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  reload: () -> Unit,
  navigateToClaimChat: () -> Unit,
) {
  val newChatSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  val startClaimBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  HedvigBottomSheet(
    newChatSelectBottomSheetState,
    content = {
      NewChatSelectBottomSheetContent(
        onNavigateToNewConversation = {
          newChatSelectBottomSheetState.dismiss()
          onNavigateToNewConversation()
        },
        onStartNewClaim = {
          newChatSelectBottomSheetState.dismiss()
          startClaimBottomSheetState.show(Unit)
        },
        dismiss = {
          newChatSelectBottomSheetState.dismiss()
        },
      )
    },
  )
  StartClaimBottomSheet(
    state = startClaimBottomSheetState,
    navigateToClaimChat = {
      startClaimBottomSheetState.dismiss()
      navigateToClaimChat()
    },
  )
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      TopAppBar(
        title = stringResource(Res.string.CHAT_CONVERSATION_INBOX),
        actionType = TopAppBarActionType.BACK,
        onActionClick = navigateUp,
        topAppBarActions = {
          if (uiState is InboxUiState.Success && uiState.newConversationButtonAvailable) {
            NewConversationButton(
              {
                newChatSelectBottomSheetState.show(Unit)
              },
            )
          }
        },
      )
      when (uiState) {
        InboxUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()

        InboxUiState.Failure -> HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        )

        is InboxUiState.Success -> InboxSuccessScreen(
          inboxConversations = uiState.inboxConversations,
          onConversationClick = onConversationClick,
          onNavigateToNewConversation = {
            newChatSelectBottomSheetState.show(Unit)
          },
          showNewConversationButton = uiState.newConversationButtonAvailable,
        )
      }
    }
  }
}

@Composable
private fun NewConversationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerXSmall)
      .clickable(
        onClickLabel = stringResource(Res.string.HC_CHAT_BUTTON),
        role = Role.Button,
        onClick = onClick,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(HedvigIcons.PenEdit, EmptyContentDescription)
    Spacer(Modifier.width(6.dp))
    HedvigText(
      stringResource(Res.string.INBOX_NEW_MESSAGE),
    )
  }
}

@Composable
private fun NewChatSelectBottomSheetContent(
  onNavigateToNewConversation: () -> Unit,
  onStartNewClaim: () -> Unit,
  dismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigCard(
      onClick = onNavigateToNewConversation,
      modifier = Modifier
        .fillMaxWidth(),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(start = 16.dp, bottom = 14.dp, top = 12.dp, end = 12.dp),
      ) {
        HedvigText(
          text = stringResource(Res.string.CHAT_CONVERSATION_QUESTION_TITLE),
          textAlign = TextAlign.Start,
        )
        HedvigText(
          text = stringResource(Res.string.INBOX_NEW_MESSAGE_SUPPORT_DESCRIPTION),
          textAlign = TextAlign.Start,
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.finePrint,
        )
      }
    }
    Spacer(Modifier.height(4.dp))
    HedvigCard(
      onClick = onStartNewClaim,
      modifier = Modifier
        .fillMaxWidth(),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(start = 16.dp, bottom = 14.dp, top = 12.dp, end = 12.dp),
      ) {
        HedvigText(
          text = stringResource(Res.string.home_tab_claim_button_text),
          textAlign = TextAlign.Start,
        )
        HedvigText(
          text = stringResource(Res.string.INBOX_NEW_MESSAGE_CLAIM_DESCRIPTION),
          textAlign = TextAlign.Start,
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.finePrint,
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_close_button),
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      onClick = {
        dismiss()
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun InboxSuccessScreen(
  inboxConversations: List<InboxConversation>,
  onConversationClick: (id: String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  showNewConversationButton: Boolean,
) {
  val lazyListState = rememberLazyListState()
  SideEffect {
    // Keep at the top of the list if we are already at the top and there is a re-arrangement
    // https://slack-chats.kotlinlang.org/t/20209529/ujn92aya0-wave-i-have-a-reversed-lazycolumn-and-its-last-ite#e0eabbdf-ae4b-420a-9bca-2a9de96ed2bd
    if (!lazyListState.canScrollBackward && lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
      lazyListState.requestScrollToItem(0)
    }
  }
  if (inboxConversations.isNotEmpty()) {
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
          ConversationCard(
            conversation = conversation,
            onConversationClick = onConversationClick,
            modifier = Modifier.horizontalDivider(DividerPosition.Top, show = index != 0),
          )
        }
      }
    }
  } else {
    Column(
      Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      EmptyState(
        text = stringResource(Res.string.INBOX_EMPTY_STATE_TITLE),
        description = if (showNewConversationButton) stringResource(Res.string.INBOX_EMPTY_STATE_SUBTITLE) else null,
        iconStyle = EmptyStateDefaults.EmptyStateIconStyle.NO_ICON,
        buttonStyle = if (showNewConversationButton) {
          EmptyStateDefaults.EmptyStateButtonStyle.Button(
            stringResource(Res.string.open_chat),
            onNavigateToNewConversation,
          )
        } else {
          EmptyStateDefaults.EmptyStateButtonStyle.NoButton
        },
      )
    }
  }
}

@Composable
private fun ConversationCard(
  conversation: InboxConversation,
  onConversationClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val title = when (conversation.header) {
    Header.Legacy -> stringResource(Res.string.CHAT_CONVERSATION_HISTORY_TITLE)
    is Header.ClaimConversation -> stringResource(Res.string.home_claim_card_pill_claim)
    Header.ServiceConversation -> stringResource(Res.string.CHAT_CONVERSATION_QUESTION_TITLE)
  }
  val subtitle = when (val header = conversation.header) {
    Header.Legacy -> null
    is Header.ClaimConversation -> header.claimType
    Header.ServiceConversation -> null
  }
  val formattedVoiceDescription = formatInstantForTalkBack(LocalContext.current, conversation.lastMessageTimestamp)
  val cardVoiceDescription = stringResource(
    Res.string.TALKBACK_CONVERSATION_DESCRIPTION,
    "$title, ${subtitle ?: ""}",
    formattedVoiceDescription,
  )
  Surface(
    modifier = modifier.semantics(mergeDescendants = true) {
      contentDescription = cardVoiceDescription
    },
    onClick = { onConversationClick(conversation.conversationId) },
    color = if (conversation.hasNewMessages) {
      HedvigTheme.colorScheme.surfacePrimary
    } else {
      HedvigTheme.colorScheme.backgroundPrimary
    },
    contentColor = HedvigTheme.colorScheme.textPrimary,
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp, bottom = 18.dp)
        .semantics {
          hideFromAccessibility()
        },
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        {
          HedvigText(
            text = title,
            style = HedvigTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.wrapContentSize(Alignment.TopStart),
          )
        },
        {
          Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            if (conversation.hasNewMessages) {
              HighlightLabel(
                stringResource(Res.string.CHAT_NEW_MESSAGE),
                HighLightSize.Small,
                HighlightColor.Blue(HighlightShade.LIGHT),
              )
            } else if (conversation.isClosed) {
              HighlightLabel(
                stringResource(Res.string.claim_status_bar_closed),
                HighLightSize.Small,
                HighlightColor.Grey(HighlightShade.LIGHT),
              )
            } else {
              val formattedLastMessageSent = conversation.lastMessageTimestamp.formattedChatDateTime(
                getLocale(),
                stringResource(Res.string.TERMINATION_FLOW_TODAY),
              )
              HedvigText(
                text = formattedLastMessageSent,
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.textSecondary
              )
            }
          }
        },
        spaceBetween = 8.dp,
      )
      if (subtitle != null) {
        HedvigText(
          text = subtitle,
          style = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textSecondary),
          modifier = Modifier.wrapContentSize(Alignment.TopStart),
        )
      }
      val latestMessage = conversation.latestMessage
      if (latestMessage != null) {
        Spacer(Modifier.height(8.dp))
        val sender = stringResource(
          when (latestMessage.sender) {
            Sender.MEMBER -> Res.string.CHAT_SENDER_MEMBER
            Sender.HEDVIG -> Res.string.HEDVIG_NAME_TEXT
            Sender.AUTOMATION -> Res.string.HEDVIG_NAME_TEXT
          },
        )
        val message = when (latestMessage) {
          is Text -> latestMessage.text.markdownToPlainText()
          is File -> stringResource(Res.string.CHAT_SENT_A_FILE)
          is Unknown -> stringResource(Res.string.CHAT_SENT_A_MESSAGE)
        }
        HedvigText(
          text = "$sender: $message",
          style = if (conversation.hasNewMessages) {
            HedvigTheme.typography.label
          } else {
            HedvigTheme.typography.label.copy(
              color = HedvigTheme.colorScheme.textSecondary,
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
@Composable
private fun EmptyInboxSuccessScreenPreview(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) case: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InboxScreen(
        InboxUiState.Success(
          listOf(),
          case,
        ),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun BottomSheetPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      NewChatSelectBottomSheetContent({}, {}, {})
    }
  }
}

@HedvigPreview
@Composable
private fun InboxSuccessScreenPreview(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) case: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
          newConversationButtonAvailable = case,
        ),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun ConversationCardPreview(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) cases: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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

private val mockInboxConversation1 = InboxConversation(
  conversationId = "1",
  header = Header.ClaimConversation("claimType"),
  latestMessage = Text(
    "You can find more details [here](https://hedvig.com/details).",
    Sender.HEDVIG,
    Clock.System.now(),
  ),
  hasNewMessages = true,
  createdAt = Clock.System.now(),
  isClosed = false,
)

private val mockInboxConversation2 = InboxConversation(
  conversationId = "2",
  header = Header.ClaimConversation("claimType"),
  latestMessage = File(Sender.AUTOMATION, Clock.System.now()),
  hasNewMessages = false,
  createdAt = Clock.System.now(),
  isClosed = false,
)

private val mockInboxConversation3 = InboxConversation(
  conversationId = "3",
  header = Header.ServiceConversation,
  latestMessage = Text ("Thank you! Happy to hear that!",Sender.MEMBER, Clock.System.now()),
  hasNewMessages = false,
  createdAt = Clock.System.now(),
  isClosed = true,
)

private val mockInboxConversationLegacy = InboxConversation(
  conversationId = "999",
  header = Header.Legacy,
  latestMessage = File(Sender.MEMBER, Clock.System.now()),
  hasNewMessages = true,
  createdAt = Clock.System.now(),
  isClosed = false,
)
