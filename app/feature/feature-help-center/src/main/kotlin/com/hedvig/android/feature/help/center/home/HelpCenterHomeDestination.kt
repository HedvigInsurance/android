package com.hedvig.android.feature.help.center.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.hedvig.android.feature.help.center.HelpCenterEvent
import com.hedvig.android.feature.help.center.HelpCenterUiState
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import com.hedvig.android.logger.logcat
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
  onNavigateToCommonClaim: (CommonClaim) -> Unit,
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
    onNavigateToCommonClaim = onNavigateToCommonClaim,
    onQuickActionsSelected = {
      logcat { "mariia: viewModel.emit(HelpCenterEvent.OnQuickActionSelected($it))" }
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
  onNavigateToCommonClaim: (CommonClaim) -> Unit,
  onDismissQuickActionDialog: () -> Unit,
  openChat: () -> Unit,
  onNavigateUp: () -> Unit,
) {
  logcat { "mariia: HelpCenterHomeScreen selectedQuickAction is $selectedQuickAction" }
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
      logcat { "mariia: onNavigateToQuickLink(selectedQuickAction.quickLinkDestination)" }
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
        AnimatedContent(
          targetState = quickLinksUiState,
          transitionSpec = {
            (
              fadeIn(animationSpec = tween(300))
                .togetherWith(
                  fadeOut(animationSpec = tween(300)),
                )
            )
          },
        ) {
          when (it) {
            HelpCenterUiState.QuickLinkUiState.Loading -> {
              HelpCenterSection(
                title = stringResource(R.string.HC_QUICK_ACTIONS_TITLE),
                chipContainerColor = MaterialTheme.colorScheme.typeContainer,
                contentColor = MaterialTheme.colorScheme.onTypeContainer,
                content = {
                  Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    PlaceholderQuickLinks()
                  }
                },
              )
            }

            HelpCenterUiState.QuickLinkUiState.NoQuickLinks -> {}

            is HelpCenterUiState.QuickLinkUiState.QuickLinks -> {
              HelpCenterSection(
                title = stringResource(R.string.HC_QUICK_ACTIONS_TITLE),
                chipContainerColor = MaterialTheme.colorScheme.typeContainer,
                contentColor = MaterialTheme.colorScheme.onTypeContainer,
                content = {
                  Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    for (quickLink in it.quickLinks) {
                      HedvigCard(
                        onClick = {
                          when (quickLink) {
                            is HelpCenterUiState.QuickLinkType.CommonClaimType -> onNavigateToCommonClaim(
                              quickLink.commonClaim, // todo: remove along with commonClaim
                            )

                            is HelpCenterUiState.QuickLinkType.QuickActionType -> onQuickActionsSelected(
                              quickLink.quickAction,
                            )
                          }
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 16.dp)
                          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                      ) {
                        Column(
                          verticalArrangement = Arrangement.Center,
                        ) {
                          Spacer(modifier = Modifier.height(12.dp))
                          Text(
                            text = when (quickLink) {
                              is HelpCenterUiState.QuickLinkType.CommonClaimType -> quickLink.commonClaim.title // todo: remove along with commonClaim
                              is HelpCenterUiState.QuickLinkType.QuickActionType -> stringResource(
                                quickLink.quickAction.titleRes,
                              )
                            },
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 16.dp),
                          )
                          Spacer(modifier = Modifier.height(4.dp))
                          Text(
                            text = when (quickLink) {
                              is HelpCenterUiState.QuickLinkType.CommonClaimType -> { // todo: remove along with commonClaim
                                val hintTextRes = quickLink.commonClaim.hintTextRes
                                if (hintTextRes != null) stringResource(hintTextRes) else ""
                              }

                              is HelpCenterUiState.QuickLinkType.QuickActionType -> stringResource(
                                quickLink.quickAction.hintTextRes,
                              )
                            },
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                          )
                          Spacer(modifier = Modifier.height(12.dp))
                        }
                      }
                    }
                  }
                },
              )
            }
          }
        }
        Spacer(Modifier.height(48.dp))
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
private fun PlaceholderQuickLinks() {
  List(5) {
    HedvigCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
      ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "HHHHHH",
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .placeholder(
              visible = true,
              highlight = PlaceholderHighlight.fade(),
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "HHHHHHHHHHHHHHHHHH",
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .placeholder(true, highlight = PlaceholderHighlight.fade()),
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        selectedQuickAction = null,
        onNavigateToTopic = {},
        onNavigateToQuestion = {},
        onNavigateToQuickLink = {},
        onQuickActionsSelected = {},
        onNavigateToCommonClaim = {},
        onDismissQuickActionDialog = {},
        openChat = {},
        onNavigateUp = {},
        quickLinksUiState = HelpCenterUiState.QuickLinkUiState.QuickLinks(
          List(3) {
            HelpCenterUiState.QuickLinkType.CommonClaimType(
              CommonClaim.Generic(
                "$it",
                "Long displayName 1234567",
                12,
                emptyList(),
              ),
            )
          }.plus(HelpCenterUiState.QuickLinkType.QuickActionType(QuickAction.MultiSelectQuickLink(0, 0, emptyList())))
            .toPersistentList(),
        ),
      )
    }
  }
}
