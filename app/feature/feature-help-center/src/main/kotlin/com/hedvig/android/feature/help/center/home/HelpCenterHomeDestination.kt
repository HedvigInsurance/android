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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.DialogDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionRightAligned
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Search
import com.hedvig.android.design.system.hedvig.placeholder.fade
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.feature.help.center.HelpCenterEvent
import com.hedvig.android.feature.help.center.HelpCenterUiState
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.data.FAQTopic
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.QuickAction.MultiSelectExpandedLink
import com.hedvig.android.feature.help.center.model.QuickAction.MultiSelectQuickLink
import com.hedvig.android.feature.help.center.model.QuickAction.StandaloneQuickLink
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.R

@Composable
internal fun HelpCenterHomeDestination(
  viewModel: HelpCenterViewModel,
  onNavigateToTopic: (topicId: String) -> Unit,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.destinationToNavigate) {
    val destination = uiState.destinationToNavigate
    if (destination != null && uiState.selectedQuickAction == null) {
      viewModel.emit(HelpCenterEvent.ClearNavigation)
      onNavigateToQuickLink(destination)
    }
  }
  HelpCenterHomeScreen(
    topics = uiState.topics,
    questions = uiState.questions,
    quickLinksUiState = uiState.quickLinksUiState,
    selectedQuickAction = uiState.selectedQuickAction,
    onNavigateToTopic = onNavigateToTopic,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateToQuickLink = {
      viewModel.emit(HelpCenterEvent.NavigateToQuickAction(it))
    },
    onQuickActionsSelected = {
      viewModel.emit(HelpCenterEvent.OnQuickActionSelected(it))
    },
    onDismissQuickActionDialog = {
      viewModel.emit(HelpCenterEvent.OnDismissQuickActionDialog)
    },
    showNavigateToInboxButton = uiState.showNavigateToInboxButton,
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
    onNavigateUp = onNavigateUp,
    search = uiState.search,
    onUpdateSearchResults = { searchQuery, helpSearchResults ->
      viewModel.emit(HelpCenterEvent.UpdateSearchResults(searchQuery, helpSearchResults))
    },
    onClearSearch = {
      viewModel.emit(HelpCenterEvent.ClearSearchQuery)
    },
  )
}

@Composable
private fun HelpCenterHomeScreen(
  search: HelpCenterUiState.Search?,
  topics: List<FAQTopic>,
  questions: List<FAQItem>,
  quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
  selectedQuickAction: QuickAction?,
  onNavigateToTopic: (topicId: String) -> Unit,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination) -> Unit,
  onQuickActionsSelected: (QuickAction) -> Unit,
  onDismissQuickActionDialog: () -> Unit,
  showNavigateToInboxButton: Boolean,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onUpdateSearchResults: (String, HelpCenterUiState.HelpSearchResults?) -> Unit,
  onClearSearch: () -> Unit,
) {
  when (selectedQuickAction) {
    is MultiSelectQuickLink -> {
      var chosenIndex by remember { mutableStateOf<Int?>(null) }
      val entries = buildList {
        selectedQuickAction.links.forEachIndexed { index, quickLink ->
          add(
            RadioOptionData(
              id = quickLink.hashCode().toString(),
              chosenState = if (index == chosenIndex) Chosen else NotChosen,
              optionText = quickLink.displayName,
            ),
          )
        }
      }
      SingleSelectDialog(
        onDismissRequest = onDismissQuickActionDialog,
        title = stringResource(id = selectedQuickAction.titleRes),
        optionsList = entries,
        onSelected = { data ->
          val chosen = entries.indexOf(data)
          chosenIndex = chosen
          onDismissQuickActionDialog()
          onNavigateToQuickLink(selectedQuickAction.links[chosen].quickLinkDestination)
        },
      )
    }

    is StandaloneQuickLink -> {
      LaunchedEffect(Unit) {
        onDismissQuickActionDialog()
        onNavigateToQuickLink(selectedQuickAction.quickLinkDestination)
      }
    }

    is MultiSelectExpandedLink -> {
      HedvigDialog(
        applyDefaultPadding = false,
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
          onDismissQuickActionDialog()
        },
        style = DialogDefaults.DialogStyle.NoButtons,
      ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
          var selectedIndex by remember { mutableStateOf<Int?>(null) }
          Spacer(Modifier.height(24.dp))
          HedvigText(
            stringResource(R.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.height(24.dp))
          selectedQuickAction.links.forEachIndexed { index, standaloneQuickLink ->
            RadioOptionRightAligned(
              chosenState = if (index == selectedIndex) Chosen else NotChosen,
              onClick = {
                selectedIndex = index
              },
              optionContent = {
                Column {
                  HedvigText(
                    text = stringResource(
                      standaloneQuickLink.titleRes,
                    ),
                  )
                  HedvigText(
                    text = stringResource(
                      standaloneQuickLink.hintTextRes,
                    ),
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
                  )
                }
              },
            )
            if (index != selectedQuickAction.links.lastIndex) {
              Spacer(Modifier.height(4.dp))
            }
          }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            text = stringResource(R.string.general_continue_button),
            enabled = selectedIndex != null,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              selectedIndex?.let { index ->
                onNavigateToQuickLink(selectedQuickAction.links[index].quickLinkDestination)
              }
            },
          )
          Spacer(Modifier.height(4.dp))
          HedvigTextButton(
            buttonSize = Large,
            text = stringResource(R.string.general_cancel_button),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              selectedIndex = null
              onDismissQuickActionDialog()
            },
          )
          Spacer(Modifier.height(24.dp))
        }
      }
    }

    null -> {}
  }
  var searchQuery by remember {
    mutableStateOf<String?>(search?.searchQuery)
  }
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.clearFocusOnTap(),
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
              questionsForSearch = topics.flatMap { it.commonFAQ + it.otherFAQ }
            )
            onUpdateSearchResults(it, results)
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
            fadeIn(animationSpec = tween(220, delayMillis = 90))
              .togetherWith(fadeOut(animationSpec = tween(90)))
          },
        ) { animatedSearch ->
          if (animatedSearch == null) {
            ContentWithoutSearch(
              quickLinksUiState = quickLinksUiState,
              onQuickActionsSelected = onQuickActionsSelected,
              topics = topics,
              onNavigateToTopic = onNavigateToTopic,
              questions = questions,
              onNavigateToQuestion = onNavigateToQuestion,
              showNavigateToInboxButton = showNavigateToInboxButton,
              onNavigateToInbox = onNavigateToInbox,
              onNavigateToNewConversation = onNavigateToNewConversation,
            )
          } else {
            SearchResults(
              activeSearchState = animatedSearch.activeSearchState,
              onBackPressed = {
                searchQuery = null
                onClearSearch()
              },
              onNavigateToQuestion = onNavigateToQuestion,
              onQuickActionsSelected = onQuickActionsSelected,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ContentWithoutSearch(
  quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
  onQuickActionsSelected: (QuickAction) -> Unit,
  topics: List<FAQTopic>,
  onNavigateToTopic: (topicId: String) -> Unit,
  questions: List<FAQItem>,
  onNavigateToQuestion: (questionId: String) -> Unit,
  showNavigateToInboxButton: Boolean,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  Column {
    Column(
      modifier =
        Modifier.padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()),
    ) {
      Spacer(Modifier.height(32.dp))
      Image(
        painter = painterResource(id = R.drawable.pillow_hedvig),
        contentDescription = null,
        modifier = Modifier
          .size(170.dp)
          .align(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(50.dp))
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .padding(horizontal = 20.dp),
      ) {
        HedvigText(stringResource(id = R.string.HC_HOME_VIEW_QUESTION))
        HedvigText(
          text = stringResource(id = R.string.HC_HOME_VIEW_ANSWER),
          color = HedvigTheme.colorScheme.textSecondary,
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
        AnimatedVisibility(!topics.isEmpty()) {
          Column {
            HelpCenterSection(
              modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
              title = stringResource(id = R.string.HC_COMMON_TOPICS_TITLE),
              chipContainerColor = HighlightColor.Yellow(LIGHT),
              content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  for (topic in topics) {
                    HedvigCard(
                      onClick = { onNavigateToTopic(topic.id) },
                      modifier = Modifier
                        .fillMaxWidth(),
                    ) {
                      HedvigText(topic.title, Modifier.padding(16.dp))
                    }
                  }
                }
              },
            )
            Spacer(Modifier.height(32.dp))
          }

        }
        LocalConfiguration.current
        AnimatedVisibility(
          !questions.isEmpty()
        ) {
          HelpCenterSectionWithClickableRows(
            modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
            title = stringResource(id = R.string.HC_COMMON_QUESTIONS_TITLE),
            chipContainerColor = HighlightColor.Blue(LIGHT),
            items = questions,
            itemText = { it.question },
            onClickItem = { onNavigateToQuestion(it.id) },
          )
        }
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(40.dp))
    StillNeedHelpSection(
      onNavigateToInbox = onNavigateToInbox,
      onNavigateToNewConversation = onNavigateToNewConversation,
      showNavigateToInboxButton = showNavigateToInboxButton,
      contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues() +
        PaddingValues(horizontal = 16.dp),
    )
  }
}

@Composable
private fun SearchResults(
  activeSearchState: HelpCenterUiState.ActiveSearchState,
  onBackPressed: () -> Unit,
  onNavigateToQuestion: (questionId: String) -> Unit,
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
        HedvigText(
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          text = stringResource(R.string.SEARCH_NOTHING_FOUND),
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    is HelpCenterUiState.ActiveSearchState.Success -> {
      Column(
        Modifier.padding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal).asPaddingValues() +
            PaddingValues(horizontal = 16.dp),
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
            chipContainerColor = HighlightColor.Green(LIGHT),
            items = activeSearchState.results.filteredQuickLinks,
            itemText = { resources.getString(it.quickAction.titleRes) },
            itemSubtitle = { resources.getString(it.quickAction.hintTextRes) },
            onClickItem = { onQuickActionsSelected(it.quickAction) },
          )
          Spacer(Modifier.height(32.dp))
        }
        if (activeSearchState.results.filteredQuestions != null) {
          HelpCenterSectionWithClickableRows(
            title = stringResource(R.string.HC_COMMON_QUESTIONS_TITLE),
            chipContainerColor = HighlightColor.Blue(LIGHT),
            items = activeSearchState.results.filteredQuestions,
            itemText = { it.question },
            onClickItem = { onNavigateToQuestion(it.id) },
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
        color = HedvigTheme.colorScheme.surfacePrimary,
        shape = HedvigTheme.shapes.cornerMedium,
      ),
  ) {
    BasicTextField(
      value = searchQuery ?: "",
      onValueChange = onSearchChange,
      cursorBrush = SolidColor(HedvigTheme.colorScheme.fillPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester),
      textStyle = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textPrimary),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(
        onSearch = {
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
            HedvigIcons.Search,
            contentDescription = null,
            modifier = Modifier
              .alpha(0.60f)
              .padding(8.dp)
              .size(24.dp),
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (searchQuery.isNullOrEmpty()) {
              HedvigText(
                text = stringResource(R.string.SEARCH_PLACEHOLDER),
                style = HedvigTheme.typography.bodySmall.copy(color = LocalContentColor.current),
                modifier = Modifier
                  .alpha(0.60f),
              )
            }
            innerTextField()
          }
          ClearSearchIcon(
            onClick = onClearSearch,
            tint = HedvigTheme.colorScheme.fillPrimary,
            modifier = Modifier
              .padding(8.dp)
              .size(24.dp)
              .then(
                if (searchQuery.isNullOrEmpty()) {
                  Modifier.withoutPlacement()
                } else {
                  Modifier
                },
              ),
          )
        }
      },
    )
  }
}

@Composable
private fun ClearSearchIcon(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
  ) {
    Icon(
      HedvigIcons.Close,
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
    modifier = Modifier.padding(horizontal = 16.dp),
    title = stringResource(R.string.HC_QUICK_ACTIONS_TITLE),
    chipContainerColor = HighlightColor.Green(LIGHT),
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
                  HedvigText(
                    text = stringResource(
                      quickLink.quickAction.titleRes,
                    ),
                    textAlign = TextAlign.Start,
                  )
                },
                bottomText = {
                  HedvigText(
                    text = stringResource(
                      quickLink.quickAction.hintTextRes,
                    ),
                    textAlign = TextAlign.Start,
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
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
          HedvigText(
            text = "HHHHHH",
            modifier = Modifier
              .hedvigPlaceholder(
                visible = true,
                shape = HedvigTheme.shapes.cornerSmall,
                highlight = PlaceholderHighlight.fade(),
              ),
          )
        },
        bottomText = {
          HedvigText(
            text = "HHHHHHHHHHHHHHHHHH",
            style = HedvigTheme.typography.label,
            modifier = Modifier
              .hedvigPlaceholder(
                true,
                shape = HedvigTheme.shapes.cornerSmall,
                highlight = PlaceholderHighlight.fade(),
              ),
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
      .fillMaxWidth(),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
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
  questionsForSearch: List<FAQItem>,
  context: Context,
): HelpCenterUiState.HelpSearchResults? {
  val lowercased = query.lowercase()
  val resultsInQuickLinks =
    buildList {
      for (link in quickLinksForSearch) {
        val title = context.getString(link.quickAction.titleRes).lowercase()
        val hint = context.getString(link.quickAction.hintTextRes).lowercase()
        if (title.contains(lowercased) || hint.contains(lowercased)) {
          add(link)
        }
      }
    }.toNonEmptyListOrNull()
  val resultsInQuestions = buildList {
    questionsForSearch.forEach {
      val answer = it.answer.lowercase()
      val question = it.question.lowercase()
      if (answer.contains(lowercased) || question.contains(lowercased)) {
        add(it)
      }
    }
  }.toNonEmptyListOrNull()
  return if (resultsInQuestions == null && resultsInQuickLinks == null) {
    null
  } else {
    HelpCenterUiState.HelpSearchResults(resultsInQuickLinks,
      resultsInQuestions)
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterHomeScreen(
  @PreviewParameter(QuickLinkUiStatePreviewProvider::class) quickLinksUiState: HelpCenterUiState.QuickLinkUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterHomeScreen(
        topics = listOf(FAQTopic(
          title = "Payments",
          commonFAQ = listOf(),
          otherFAQ = listOf(),
          id = "topicId"
        )),
        questions = listOf(FAQItem(
          "01",
          stringResource(R.string.HC_CLAIMS_Q_01),
          stringResource(R.string.HC_CLAIMS_A_01),
        )),
        selectedQuickAction = null,
        onNavigateToTopic = {},
        onNavigateToQuestion = {},
        onNavigateToQuickLink = {},
        onQuickActionsSelected = {},
        onDismissQuickActionDialog = {},
        showNavigateToInboxButton = true,
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        onNavigateUp = {},
        quickLinksUiState = quickLinksUiState,
        onClearSearch = {},
        onUpdateSearchResults = { _, _ -> },
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        parametersList = provider.values.toList(),
      ) { quickLinkUiState ->
        HelpCenterHomeScreen(
          topics = listOf(FAQTopic(
            title = "Payments",
            commonFAQ = listOf(),
            otherFAQ = listOf(),
            id = "topicId"
          )),
          questions = listOf(FAQItem(
            "01",
            stringResource(R.string.HC_CLAIMS_Q_01),
            stringResource(R.string.HC_CLAIMS_A_01),
          )),
          selectedQuickAction = null,
          onNavigateToTopic = {},
          onNavigateToQuestion = {},
          onNavigateToQuickLink = {},
          onQuickActionsSelected = {},
          onDismissQuickActionDialog = {},
          showNavigateToInboxButton = true,
          onNavigateToInbox = {},
          onNavigateToNewConversation = {},
          onNavigateUp = {},
          quickLinksUiState = quickLinkUiState,
          onClearSearch = {},
          onUpdateSearchResults = { _, _ -> },
          search = null,
        )
      }
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
                StandaloneQuickLink(
                  R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
                  R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
                  QuickLinkDestination.OuterDestination.QuickLinkTermination,
                ),
              )
            },
          )
          add(
            HelpCenterUiState.QuickLink(
              MultiSelectQuickLink(
                R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                emptyList(),
              ),
            ),
          )
        }.toNonEmptyListOrNull()!!,
      ),
    ),
  )

@Preview
@Composable
private fun SearchFieldPreview() {
  SearchField("travel", FocusRequester(), {}, {}, {})
}
