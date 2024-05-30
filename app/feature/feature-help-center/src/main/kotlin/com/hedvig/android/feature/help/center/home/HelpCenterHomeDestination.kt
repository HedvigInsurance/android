package com.hedvig.android.feature.help.center.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.onYellowContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.yellowContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.MultiSelectDialog
import com.hedvig.android.core.ui.layout.LayoutWithoutPlacement
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
    onUpdateSearchResults = { helpSearchResults ->
      viewModel.emit(HelpCenterEvent.UpdateSearchResults(helpSearchResults))
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
  onUpdateSearchResults: (HelpCenterUiState.HelpSearchResults?) -> Unit,
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
    mutableStateOf<String?>(null)
  }
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.clickable(
      indication = null,
      interactionSource = remember { MutableInteractionSource() },
    ) {
      focusManager.clearFocus()
    },
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      Spacer(modifier = Modifier.height(8.dp))
      val context = LocalContext.current
      SearchField(
        searchQuery = searchQuery,
        focusRequester = focusRequester,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
          ),
        onSearchChange = {
          if (it.isEmpty()) {
            searchQuery = null
            onClearSearch()
          } else {
            searchQuery = it
            val results = searchForQuery(
              query = it,
              context = context,
              quickLinksForSearch = (
                quickLinksUiState as?
                  HelpCenterUiState.QuickLinkUiState.QuickLinks
              )?.quickLinks ?: listOf(),
            )
            onUpdateSearchResults(results)
          }
        },
        onKeyboardAction = {
          searchQuery?.let {
            focusManager.clearFocus()
          }
        },
        onClearSearch = {
          searchQuery = null
          onClearSearch()
        },
      )
      Spacer(Modifier.height(16.dp))
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        AnimatedContent(
          targetState = search,
          transitionSpec = {
            (
              fadeIn(
                animationSpec = tween(220, delayMillis = 90),
              )
            ).togetherWith(fadeOut(animationSpec = tween(90)))
          },
        ) { animatedSearch ->
          if (animatedSearch == null) {
            Column {
              Spacer(Modifier.height(32.dp))
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
              Spacer(Modifier.height(24.dp))

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
              Spacer(Modifier.weight(1f))
              Spacer(Modifier.height(40.dp))
              StillNeedHelpSection(
                openChat = openChat,
                contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
              )
            }
          } else {
            SearchResults(
              animatedSearch.activeSearchState,
              onBackPressed = {
                searchQuery = null
                onClearSearch()
              },
              onNavigateToQuestion,
              onQuickActionsSelected,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SearchResults(
  activeSearchState: HelpCenterUiState.ActiveSearchState,
  onBackPressed: () -> Unit,
  onNavigateToQuestion: (question: Question) -> Unit,
  onQuickActionsSelected: (QuickAction) -> Unit,
) {
  BackHandler(true) {
    onBackPressed()
  }

  when (activeSearchState) {
    HelpCenterUiState.ActiveSearchState.Empty -> {
      Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          text = "Nothing found, sorry!",
        ) // todo: remove hardcode
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    is HelpCenterUiState.ActiveSearchState.Success -> {
      Column(
        Modifier.padding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
        ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        LocalConfiguration.current
        val resources = LocalContext.current.resources
        if (activeSearchState.results.filteredQuickLinks != null) {
          HelpCenterSectionWithClickableRows(
            title = stringResource(R.string.HC_QUICK_ACTIONS_TITLE),
            chipContainerColor = MaterialTheme.colorScheme.typeContainer,
            contentColor = MaterialTheme.colorScheme.onTypeContainer,
            items = activeSearchState.results.filteredQuickLinks.toPersistentList(),
            itemText = { resources.getString(it.quickAction.titleRes) },
            itemSubtitle = { resources.getString(it.quickAction.hintTextRes) },
            onClickItem = { onQuickActionsSelected(it.quickAction) },
          )
          Spacer(Modifier.height(32.dp))
        }
        if (activeSearchState.results.filteredQuestions != null) {
          HelpCenterSectionWithClickableRows(
            title = stringResource(R.string.HC_COMMON_QUESTIONS_TITLE),
            chipContainerColor = MaterialTheme.colorScheme.infoContainer,
            contentColor = MaterialTheme.colorScheme.onInfoContainer,
            items = activeSearchState.results.filteredQuestions.toPersistentList(),
            itemText = { resources.getString(it.questionRes) },
            onClickItem = { onNavigateToQuestion(it) },
          )
          Spacer(Modifier.height(32.dp))
        }
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
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .background(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.squircleMedium,
      ),
  ) {
    BasicTextField(
      value = searchQuery ?: "",
      onValueChange = onSearchChange,
      cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester),
      textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(
        onAny = {
          onKeyboardAction()
        },
      ),
      decorationBox = { innerTextField ->
        Row(
          Modifier
            .fillMaxSize(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
              .alpha(0.60f)
              .padding(8.dp),
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 4.dp),
          ) {
            if (searchQuery.isNullOrEmpty()) {
              Text(
                text = "Search", // todo: remove hardcode
                style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                modifier = Modifier
                  .alpha(0.60f),
              )
            }
            innerTextField()
          }
          LayoutWithoutPlacement(
            sizeAdjustingContent = {
              ClearSearchIcon(
                onClearSearch,
                tint = MaterialTheme.colorScheme.onSurface,
              )
            },
          ) {
            if (!searchQuery.isNullOrEmpty()) {
              ClearSearchIcon(
                onClearSearch,
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      },
    )
  }
}

@Composable
private fun ClearSearchIcon(onClearSearch: () -> Unit, tint: Color) {
  IconButton(
    onClick = onClearSearch,
  ) {
    Icon(
      Icons.Default.Clear,
      contentDescription = null,
      tint = tint,
    )
  }
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

private fun searchForQuery(
  query: String,
  quickLinksForSearch: List<HelpCenterUiState.QuickLink>,
  context: Context,
): HelpCenterUiState.HelpSearchResults? {
  val resultsInQuickLinks =
    buildList {
      for (link in quickLinksForSearch) {
        val title = context.getString(link.quickAction.titleRes).lowercase()
        val hint = context.getString(link.quickAction.hintTextRes).lowercase()
        if (title.contains(query) || hint.contains(query)) {
          add(link)
        }
      }
    }.toNonEmptyListOrNull()
  val resultsInQuestions = buildList {
    Question.entries.forEach {
      val answer = context.getString(it.answerRes).lowercase()
      val question = context.getString(it.questionRes).lowercase()
      if (answer.contains(query) || question.contains(query)) {
        add(it)
      }
    }
  }.toNonEmptyListOrNull()
  return if (resultsInQuestions == null && resultsInQuickLinks == null) {
    null
  } else {
    HelpCenterUiState.HelpSearchResults(resultsInQuickLinks, resultsInQuestions)
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
        onUpdateSearchResults = {},
        search = null,
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
            onUpdateSearchResults = {},
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

@Preview
@Composable
private fun SearchFieldPreview() {
  SearchField("travel", FocusRequester(), {}, {}, {})
}
