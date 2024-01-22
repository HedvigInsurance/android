package com.hedvig.android.feature.help.center.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.android.feature.help.center.HelpCenterEvent
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.Destination
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun HelpCenterHomeDestination(
  viewModel: HelpCenterViewModel,
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (Destination) -> Unit,
  onNavigateToCommonClaim: (CommonClaim) -> Unit,
  onNavigateUp: () -> Unit,
  openChat: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HelpCenterHomeScreen(
    topics = uiState.topics,
    questions = uiState.questions,
    quickActions = uiState.quickLinks,
    commonClaims = uiState.commonClaims,
    selectedQuickAction = uiState.selectedQuickAction,
    onNavigateToTopic = onNavigateToTopic,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateToQuickLink = onNavigateToQuickLink,
    onNavigateToCommonClaim = onNavigateToCommonClaim,
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
  quickActions: ImmutableList<QuickAction>,
  commonClaims: ImmutableList<CommonClaim>,
  selectedQuickAction: QuickAction?,
  onNavigateToTopic: (topic: Topic) -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToQuickLink: (Destination) -> Unit,
  onQuickActionsSelected: (QuickAction) -> Unit,
  onNavigateToCommonClaim: (CommonClaim) -> Unit,
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
        onNavigateToQuickLink(it.destination)
      },
      getDisplayText = { it.displayName ?: "" },
      getIsSelected = null,
      getId = { it.hashCode().toString() },
    )

    is QuickAction.QuickLink -> {
      onDismissQuickActionDialog()
      onNavigateToQuickLink(selectedQuickAction.destination)
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
        HelpCenterSection(
          title = stringResource(id = R.string.HC_QUICK_ACTIONS_TITLE),
          chipContainerColor = MaterialTheme.colorScheme.typeContainer,
          contentColor = MaterialTheme.colorScheme.onTypeContainer,
          content = {
            AnimatedVisibility(
              visible = quickActions.isNotEmpty(),
              enter = fadeIn(),
              exit = fadeOut(),
            ) {
              HedvigGrid(
                insideGridSpace = InsideGridSpace.Companion.invoke(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
              ) {
                for (quickAction in quickActions) {
                  HedvigCard(
                    onClick = {
                      onQuickActionsSelected(quickAction)
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                  ) {
                    Column(
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                      Text(
                        text = stringResource(quickAction.titleRes),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                      )
                    }
                  }
                }
              }
            }
            AnimatedVisibility(
              visible = commonClaims.isNotEmpty(),
              enter = fadeIn(),
              exit = fadeOut(),
            ) {
              HedvigGrid(
                insideGridSpace = InsideGridSpace.Companion.invoke(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
              ) {
                for (commonClaim in commonClaims) {
                  HedvigCard(
                    onClick = {
                      onNavigateToCommonClaim(commonClaim)
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                  ) {
                    Column(
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                      Text(
                        text = commonClaim.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                      )
                    }
                  }
                }
              }
            }
          },
        )
        Spacer(Modifier.height(56.dp))
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
        Spacer(Modifier.height(56.dp))
        StillNeedHelpSection(openChat = {
          openChat()
        })
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterHomeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterHomeScreen(
        topics = persistentListOf(Topic.PAYMENTS, Topic.PAYMENTS),
        questions = persistentListOf(Question.CLAIMS_Q1, Question.CLAIMS_Q1),
        quickActions = persistentListOf(
          QuickAction.QuickLink(0, "Long displayName 1234567", AppDestination.EditCoInsured),
          QuickAction.QuickLink(
            R.string.HC_QUICK_ACTIONS_UPDATE_ADDRESS,
            "Long displayName 1234567",
            AppDestination.EditCoInsured,
          ),
        ),
        commonClaims = persistentListOf(),
        selectedQuickAction = null,
        onNavigateToTopic = {},
        onNavigateToQuestion = {},
        onNavigateToQuickLink = {},
        onQuickActionsSelected = {},
        onNavigateToCommonClaim = {},
        onDismissQuickActionDialog = {},
        openChat = {},
        onNavigateUp = {},
      )
    }
  }
}
