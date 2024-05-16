package com.hedvig.android.feature.help.center.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextFieldDefaults
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
import com.hedvig.android.core.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
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
    search = uiState.search,
    onSearchChange = { query ->
      viewModel.emit(HelpCenterEvent.SearchForQuery(query))
    },
    onClearSearch = {
      viewModel.emit(HelpCenterEvent.ClearSearchQuery)
    },
  )
}

@Composable
private fun HelpCenterHomeScreen(
  search: HelpCenterUiState.Search?,
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
  onSearchChange: (String) -> Unit,
  onClearSearch: () -> Unit,
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
  var searchQuery by remember {
    mutableStateOf(search?.query)
  }
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
          ) {
            focusManager.clearFocus() // clearing focus for search textField
          }
          .imePadding()
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
        Spacer(modifier = Modifier.height(40.dp))
        SearchField(
          searchQuery = searchQuery,
          focusRequester = focusRequester,
          onSearchChange = {
            if (it.isEmpty()) {
              searchQuery = null
              onClearSearch()
            } else {
              searchQuery = it
            }
          },
          onKeyboardAction = {
            searchQuery?.let {
              focusManager.clearFocus()
              onSearchChange(it)
            }
          },
          onClearSearch = {
            searchQuery = null
            onClearSearch()
          },
        )
        Spacer(Modifier.height(24.dp))
        AnimatedContent(targetState = search) { animatedSearch ->
          if (animatedSearch == null) {
            Column {
              AnimatedVisibility(
                visible = quickLinksUiState !is HelpCenterUiState.QuickLinkUiState.NoQuickLinks,
                enter = QuickLinksSectionEnterTransition,
                exit = QuickLinksSectionExitTransition,
              ) {
                Column {
                  QuickLinksSection(quickLinksUiState, onQuickActionsSelected)
                  Spacer(Modifier.height(32.dp))
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
              Spacer(Modifier.height(32.dp))
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
            }
          } else {
            when (animatedSearch.activeSearchState) {
              HelpCenterUiState.ActiveSearchState.Empty -> {
                Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Nothing found, sorry!",
                  ) // todo: remove hardcode
                }
              }

              HelpCenterUiState.ActiveSearchState.Loading -> {
                Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  HedvigFullScreenCenterAlignedProgress()
                }
              }

              is HelpCenterUiState.ActiveSearchState.Success -> {
                Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  if (animatedSearch.activeSearchState.results.filteredQuickLinks != null) {
                    val searchQuickLinkUiState = HelpCenterUiState.QuickLinkUiState.QuickLinks(
                      animatedSearch.activeSearchState.results.filteredQuickLinks.toPersistentList(),
                    )
                    QuickLinksSection(searchQuickLinkUiState, onQuickActionsSelected)
                    Spacer(Modifier.height(32.dp))
                  }
                  if (animatedSearch.activeSearchState.results.filteredQuestions != null) {
                    LocalConfiguration.current
                    val resources = LocalContext.current.resources
                    HelpCenterSectionWithClickableRows(
                      title = "Results in questions", // todo: hardcode
                      chipContainerColor = MaterialTheme.colorScheme.infoContainer,
                      contentColor = MaterialTheme.colorScheme.onInfoContainer,
                      items = animatedSearch.activeSearchState.results.filteredQuestions.toPersistentList(),
                      itemText = { resources.getString(it.questionRes) },
                      onClickItem = { onNavigateToQuestion(it) },
                    )
                  }
                }
              }
            }
          }
        }
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
private fun SearchField(
  searchQuery: String?,
  focusRequester: FocusRequester,
  onClearSearch: () -> Unit,
  onKeyboardAction: () -> Unit,
  onSearchChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .focusRequester(focusRequester),
    value = searchQuery ?: "",
    colors = HedvigTextFieldDefaults.colors(
      typingHighlightColor = MaterialTheme.colorScheme.surface,
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(
      onAny = {
        onKeyboardAction()
      },
    ),
    onValueChange = onSearchChange,
    placeholder = {
      Text(
        text = "Search",
        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        modifier = Modifier.alpha(0.60f),
      ) // todo: remove hardcode
    },
    leadingIcon = {
      Icon(
        Icons.Default.Search,
        contentDescription = null,
        modifier = Modifier.alpha(0.60f),
      )
    },
    trailingIcon = {
      if (searchQuery != null) {
        Icon(
          Icons.Default.Clear,
          contentDescription = null,
          modifier = Modifier.clickable {
            onClearSearch()
          },
        )
      }
    },
  )
}

private val QuickLinksSectionEnterTransition = fadeIn() + expandVertically(
  animationSpec = spring(
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = IntSize.VisibilityThreshold,
  ),
  expandFrom = Alignment.Top,
)
private val QuickLinksSectionExitTransition = fadeOut() + shrinkVertically(
  animationSpec = spring(
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = IntSize.VisibilityThreshold,
  ),
  shrinkTowards = Alignment.Top,
)

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
      AnimatedContent(
        targetState = quickLinksUiState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
      ) { quickLinks: HelpCenterUiState.QuickLinkUiState ->
        if (quickLinks is HelpCenterUiState.QuickLinkUiState.QuickLinks) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (quickLink in quickLinks.quickLinks) {
              QuickLinkCard(
                topText = {
                  Text(
                    text = stringResource(
                      quickLink.quickAction.titleRes,
                    ),
                    textAlign = TextAlign.Start,
                  )
                },
                bottomText = {
                  Text(
                    text = stringResource(
                      quickLink.quickAction.hintTextRes,
                    ),
                    textAlign = TextAlign.Start,
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
              .placeholder(visible = true, highlight = PlaceholderHighlight.fade()),
          )
        },
        bottomText = {
          Text(
            text = "HHHHHHHHHHHHHHHHHH",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
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
        onClearSearch = {},
        onSearchChange = {},
        search = null,
//        search = HelpCenterUiState.Search(
//          "dubadee",
//          HelpCenterUiState.ActiveSearchState.Success(
//            HelpCenterUiState.HelpSearchResults(
//              nonEmptyListOf(
//                HelpCenterUiState.QuickLink(
//                  QuickAction.StandaloneQuickLink(
//                    quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate,
//                    titleRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
//                    hintTextRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
//                  ),
//                ),
//              ),
//              nonEmptyListOf(
//                Question.CLAIMS_Q6,
//                Question.CLAIMS_Q9,
//                Question.COVERAGE_Q5,
//                Question.OTHER_Q4,
//              ),
//            ),
//          ),
//        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewQuickLinkAnimations() {
  val provider = QuickLinkUiStatePreviewProvider()
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        parametersList = provider.values.toList(),
        content = { quickLinkUiState ->
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
            quickLinksUiState = quickLinkUiState,
            onClearSearch = {},
            onSearchChange = {},
            search = null,
          )
        },
      )
    }
  }
}

private class QuickLinkUiStatePreviewProvider :
  CollectionPreviewParameterProvider<HelpCenterUiState.QuickLinkUiState>(
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
