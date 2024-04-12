package com.hedvig.android.feature.help.center.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.onYellowContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.yellowContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.MultiSelectDialog
import com.hedvig.android.feature.help.center.HelpCenterEvent
import com.hedvig.android.feature.help.center.HelpCenterUiState
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun HelpCenterHomeDestination(
  viewModel: HelpCenterViewModel,
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination) -> Unit,
  onNavigateUp: () -> Unit,
  openChat: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HelpCenterHomeScreen(
    topics = uiState.topics,
    questions = uiState.questions,
    quickLinksUiState = uiState.quickLinksUiState,
    selectedQuickAction = uiState.selectedQuickAction,
    onNavigateToTopic = onNavigateToTopic,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateToQuickLink = onNavigateToQuickLink,
    onQuickActionsSelected = {
      viewModel.emit(HelpCenterEvent.OnQuickActionSelected(it))
    },
    onDismissQuickActionDialog = {
      viewModel.emit(HelpCenterEvent.OnDismissQuickActionDialog)
    },
    openChat = openChat,
    onNavigateUp = onNavigateUp,
  )
}

@Composable
private fun HelpCenterHomeScreen(
  topics: ImmutableList<Topic>,
  questions: ImmutableList<Question>,
  quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
  selectedQuickAction: QuickAction?,
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination) -> Unit,
  onQuickActionsSelected: (QuickAction) -> Unit,
  onDismissQuickActionDialog: () -> Unit,
  openChat: () -> Unit,
  onNavigateUp: () -> Unit,
) {
  when (selectedQuickAction) {
    is QuickAction.MultiSelectQuickLink -> MultiSelectDialog(
      onDismissRequest = onDismissQuickActionDialog,
      title = stringResource(id = selectedQuickAction.titleRes),
      optionsList = selectedQuickAction.links,
      onSelected = {
        onDismissQuickActionDialog()
        onNavigateToQuickLink(it.quickLinkDestination)
      },
      getDisplayText = { it.displayName },
      getIsSelected = null,
      getId = { it.hashCode().toString() },
    )

    is QuickAction.StandaloneQuickLink -> {
      onDismissQuickActionDialog()
      onNavigateToQuickLink(selectedQuickAction.quickLinkDestination)
    }

    null -> {}
  }

  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        Spacer(Modifier.height(50.dp))
        Image(
          painter = painterResource(id = R.drawable.pillow_hedvig),
          contentDescription = null,
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .size(170.dp)
            .align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(50.dp))
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .padding(horizontal = 20.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
          Text(stringResource(id = R.string.HC_HOME_VIEW_QUESTION))
          Text(
            text = stringResource(id = R.string.HC_HOME_VIEW_ANSWER),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(Modifier.height(40.dp))
        AnimatedVisibility(
          visible = quickLinksUiState !is HelpCenterUiState.QuickLinkUiState.NoQuickLinks,
          enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
            expandVertically(expandFrom = Alignment.Top),
          exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
            shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
          Column {
            QuickLinksSection(quickLinksUiState, onQuickActionsSelected)
            Spacer(Modifier.height(48.dp))
          }
        }
        HelpCenterSection(
          title = stringResource(id = R.string.HC_COMMON_TOPICS_TITLE),
          chipContainerColor = MaterialTheme.colorScheme.yellowContainer,
          contentColor = MaterialTheme.colorScheme.onYellowContainer,
          content = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (topic in topics) {
                HedvigCard(
                  onClick = { onNavigateToTopic(topic) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                ) {
                  Text(stringResource(topic.titleRes), Modifier.padding(16.dp))
                }
              }
            }
          },
        )
        Spacer(Modifier.height(56.dp))
        LocalConfiguration.current
        val resources = LocalContext.current.resources
        HelpCenterSectionWithClickableRows(
          title = stringResource(id = R.string.HC_COMMON_QUESTIONS_TITLE),
          chipContainerColor = MaterialTheme.colorScheme.infoContainer,
          contentColor = MaterialTheme.colorScheme.onInfoContainer,
          items = questions,
          itemText = { resources.getString(it.questionRes) },
          onClickItem = { onNavigateToQuestion(it) },
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(40.dp))
        StillNeedHelpSection(
          openChat = openChat,
          contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
        )
      }
    }
  }
}

@Composable
private fun QuickLinksSection(
  quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
  onQuickActionsClick: (QuickAction) -> Unit,
) {
  HelpCenterSection(
    title = stringResource(R.string.HC_QUICK_ACTIONS_TITLE),
    chipContainerColor = MaterialTheme.colorScheme.typeContainer,
    contentColor = MaterialTheme.colorScheme.onTypeContainer,
    content = {
      val isQuickLinks = quickLinksUiState as? HelpCenterUiState.QuickLinkUiState.QuickLinks
      AnimatedContent(
        targetState = isQuickLinks,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
      ) { quickLinks: HelpCenterUiState.QuickLinkUiState.QuickLinks? ->
        if (quickLinks != null) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (quickLink in quickLinks.quickLinks) {
              QuickLinkCard(
                topText = {
                  Text(
                    text = stringResource(
                      quickLink.quickAction.titleRes,
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 16.dp),
                  )
                },
                bottomText = {
                  Text(
                    text = stringResource(
                      quickLink.quickAction.hintTextRes,
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                  )
                },
                onClick = {
                  onQuickActionsClick(quickLink.quickAction)
                },
              )
            }
          }
        } else {
          PlaceholderQuickLinks()
        }
      }
    },
  )
}

@Composable
private fun PlaceholderQuickLinks() {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    List(5) {
      QuickLinkCard(
        topText = {
          Text(
            text = "HHHHHH",
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .placeholder(visible = true, highlight = PlaceholderHighlight.fade()),
          )
        },
        bottomText = {
          Text(
            text = "HHHHHHHHHHHHHHHHHH",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .placeholder(true, highlight = PlaceholderHighlight.fade()),
          )
        },
      )
    }
  }
}

@Composable
private fun QuickLinkCard(
  topText: @Composable () -> Unit,
  bottomText: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(start = 16.dp, bottom = 14.dp, top = 12.dp, end = 12.dp),
    ) {
      topText()
      bottomText()
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterHomeScreen(
  @PreviewParameter(QuickLinkUiStatePreviewProvider::class) quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterHomeScreen(
        topics = persistentListOf(Topic.PAYMENTS, Topic.PAYMENTS),
        questions = persistentListOf(Question.CLAIMS_Q1, Question.CLAIMS_Q1),
        selectedQuickAction = null,
        onNavigateToTopic = {},
        onNavigateToQuestion = {},
        onNavigateToQuickLink = {},
        onQuickActionsSelected = {},
        onDismissQuickActionDialog = {},
        openChat = {},
        onNavigateUp = {},
        quickLinksUiState = quickLinksUiState,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewQuickLinkAnimations() {
  val quickLinkUiStateList: List<HelpCenterUiState.QuickLinkUiState> = remember {
    val provider = QuickLinkUiStatePreviewProvider()
    provider.values.toList() + provider.values.drop(1).toList()
  }
  var quickLinksUiStateIndex by remember { mutableIntStateOf(0) }
  HedvigTheme {
    Surface(
      onClick = {
        quickLinksUiStateIndex = quickLinksUiStateIndex + 1
      },
      color = MaterialTheme.colorScheme.background,
    ) {
      HelpCenterHomeScreen(
        topics = persistentListOf(Topic.PAYMENTS, Topic.PAYMENTS),
        questions = persistentListOf(Question.CLAIMS_Q1, Question.CLAIMS_Q1),
        selectedQuickAction = null,
        onNavigateToTopic = {},
        onNavigateToQuestion = {},
        onNavigateToQuickLink = {},
        onQuickActionsSelected = {},
        onDismissQuickActionDialog = {},
        openChat = {},
        onNavigateUp = {},
        quickLinksUiState = quickLinkUiStateList[quickLinksUiStateIndex % quickLinkUiStateList.size],
      )
    }
  }
}

private class QuickLinkUiStatePreviewProvider : CollectionPreviewParameterProvider<HelpCenterUiState.QuickLinkUiState>(
  listOf(
    HelpCenterUiState.QuickLinkUiState.NoQuickLinks,
    HelpCenterUiState.QuickLinkUiState.Loading,
    HelpCenterUiState.QuickLinkUiState.QuickLinks(
      buildList {
        addAll(
          List(3) {
            HelpCenterUiState.QuickLink(
              QuickAction.StandaloneQuickLink(
                R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
                R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
                QuickLinkDestination.OuterDestination.QuickLinkTermination,
              ),
            )
          },
        )
        add(
          HelpCenterUiState.QuickLink(
            QuickAction.MultiSelectQuickLink(
              R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
              R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
              emptyList(),
            ),
          ),
        )
      }.toPersistentList(),
    ),
  ),
)
