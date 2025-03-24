package com.hedvig.android.feature.help.center.topic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.data.FAQTopic
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicUiState.Failure
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicUiState.Loading
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicUiState.NoTopicFound
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicUiState.Success
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R

@Composable
internal fun HelpCenterTopicDestination(
  showNavigateToInboxViewModel: ShowNavigateToInboxViewModel,
  helpCenterTopicViewModel: HelpCenterTopicViewModel,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  val uiState by helpCenterTopicViewModel.uiState.collectAsStateWithLifecycle()
  val showNavigateToInboxButton by showNavigateToInboxViewModel.uiState.collectAsStateWithLifecycle()
  HelpCenterTopicScreen(
    uiState = uiState,
    showNavigateToInboxButton = showNavigateToInboxButton,
    onNavigateUp = onNavigateUp,
    onReload = { helpCenterTopicViewModel.emit(HelpCenterTopicEvent.Reload) },
    onNavigateBack = onNavigateBack,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@Composable
private fun HelpCenterTopicScreen(
  uiState: HelpCenterTopicUiState,
  showNavigateToInboxButton: Boolean,
  onNavigateUp: () -> Unit,
  onReload: () -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    val state = uiState
    val title = if (state is Success) {
      state.topic.title
    } else {
      stringResource(R.string.HC_TITLE)
    }
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = title,
        onClick = onNavigateUp,
      )
      when (state) {
        Failure -> {
          FailureScreen(
            onClick = onReload,
            errorText = stringResource(R.string.GENERAL_ERROR_BODY),
            buttonText = stringResource(R.string.GENERAL_RETRY),
          )
        }

        NoTopicFound -> {
          FailureScreen(
            onClick = dropUnlessResumed { onNavigateBack() },
            errorText = stringResource(R.string.HC_TOPIC_NOT_FOUND),
            buttonText = stringResource(R.string.general_back_button),
          )
        }

        Loading -> HedvigFullScreenCenterAlignedProgress()
        is Success -> {
          HelpCenterTopicScreen(
            commonQuestions = state.topic.commonFAQ,
            allQuestions = state.topic.commonFAQ + state.topic.otherFAQ,
            showNavigateToInboxButton = showNavigateToInboxButton,
            onNavigateToQuestion = onNavigateToQuestion,
            onNavigateToInbox = onNavigateToInbox,
            onNavigateToNewConversation = onNavigateToNewConversation,
            onNavigateBack = onNavigateBack,
          )
        }
      }
    }
  }
}

@Composable
private fun FailureScreen(onClick: () -> Unit, errorText: String, buttonText: String) {
  HedvigErrorSection(
    onButtonClick = onClick,
    title = errorText,
    subTitle = null,
    buttonText = buttonText,
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .windowInsetsPadding(
        WindowInsets.safeDrawing.only(
          WindowInsetsSides.Horizontal +
            WindowInsetsSides.Bottom,
        ),
      ),
  )
}

@Composable
private fun HelpCenterTopicScreen(
  commonQuestions: List<FAQItem>,
  allQuestions: List<FAQItem>,
  showNavigateToInboxButton: Boolean,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  Column {
    if (commonQuestions.isEmpty() && allQuestions.isEmpty()) {
      HedvigErrorSection(
        onButtonClick = dropUnlessResumed { onNavigateBack() },
        title = stringResource(id = R.string.HC_TOPIC_NO_QUESTIONS),
        subTitle = null,
        buttonText = stringResource(R.string.general_back_button),
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      )
    } else {
      LocalConfiguration.current
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      ) {
        Column(
          modifier = Modifier
            .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()),
        ) {
          Spacer(Modifier.height(16.dp))
          if (commonQuestions.isNotEmpty()) {
            HelpCenterSectionWithClickableRows(
              modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
              title = stringResource(id = R.string.HC_COMMON_QUESTIONS_TITLE),
              chipContainerColor = HighlightColor.Blue(LIGHT),
              items = commonQuestions,
              itemText = { it.question },
              onClickItem = { onNavigateToQuestion(it.id) },
            )
          }
          if (commonQuestions.isNotEmpty() && allQuestions.isNotEmpty()) {
            Spacer(Modifier.height(40.dp))
          }
          if (allQuestions.isNotEmpty()) {
            HelpCenterSectionWithClickableRows(
              modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
              title = stringResource(id = R.string.HC_ALL_QUESTION_TITLE),
              chipContainerColor = HighlightColor.Purple(LIGHT),
              items = allQuestions,
              itemText = { it.question },
              onClickItem = { onNavigateToQuestion(it.id) },
            )
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
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterTopicScreen(
  @PreviewParameter(HelpCenterTopicUiStateProvider::class) uiState: HelpCenterTopicUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterTopicScreen(
        uiState,
        false,
        {},
        {},
        {},
        { _ -> },
        {},
        {},
      )
    }
  }
}

private class HelpCenterTopicUiStateProvider : CollectionPreviewParameterProvider<HelpCenterTopicUiState>(
  listOf(
    HelpCenterTopicUiState.Loading,
    HelpCenterTopicUiState.Failure,
    HelpCenterTopicUiState.NoTopicFound,
    HelpCenterTopicUiState.Success(
      FAQTopic("id", "title", emptyList(), emptyList()),
    ),
    HelpCenterTopicUiState.Success(
      FAQTopic(
        "id",
        "title",
        List(2) {
          FAQItem(
            "$it",
            "Question $it",
            "Answer $it",
          )
        },
        List(4) {
          FAQItem(
            "$it",
            "Question $it",
            "Answer $it",
          )
        },
      ),
    ),
  ),
)
