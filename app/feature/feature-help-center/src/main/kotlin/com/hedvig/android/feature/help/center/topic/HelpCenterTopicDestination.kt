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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.Question.CLAIMS_Q1
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R

@Composable
internal fun HelpCenterTopicDestination(
  showNavigateToInboxViewModel: ShowNavigateToInboxViewModel,
  topic: Topic,
  onNavigateToQuestion: (question: Question) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  val commonQuestions = topic
    .commonQuestionIds
    .mapNotNull { questionId ->
      Question.entries.find { it == questionId }
    }

  val allQuestions = topic
    .allQuestionIds
    .mapNotNull { questionId ->
      Question.entries.find { it == questionId }
    }

  HelpCenterTopicScreen(
    topic = topic,
    commonQuestions = commonQuestions,
    allQuestions = allQuestions,
    showNavigateToInboxButton = showNavigateToInboxViewModel.uiState.collectAsStateWithLifecycle().value,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateUp = onNavigateUp,
    onNavigateBack = onNavigateBack,
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@Composable
private fun HelpCenterTopicScreen(
  topic: Topic?,
  commonQuestions: List<Question>,
  allQuestions: List<Question>,
  showNavigateToInboxButton: Boolean,
  onNavigateToQuestion: (questionId: Question) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = topic?.titleRes?.let { stringResource(it) } ?: stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      if (topic == null) {
        HedvigErrorSection(
          onButtonClick = onNavigateBack,
          title = stringResource(id = R.string.HC_TOPIC_NOT_FOUND),
          subTitle = null,
          buttonText = stringResource(R.string.general_back_button),
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
        )
      } else if (commonQuestions.isEmpty() && allQuestions.isEmpty()) {
        HedvigErrorSection(
          onButtonClick = onNavigateBack,
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
        val resources = LocalContext.current.resources
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        ) {
          Spacer(Modifier.height(16.dp))
          if (commonQuestions.isNotEmpty()) {
            HelpCenterSectionWithClickableRows(
              modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
              title = stringResource(id = R.string.HC_COMMON_QUESTIONS_TITLE),
              chipContainerColor = HighlightColor.Blue(LIGHT),
              items = commonQuestions,
              itemText = { resources.getString(it.questionRes) },
              onClickItem = { onNavigateToQuestion(it) },
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
              itemText = { resources.getString(it.questionRes) },
              onClickItem = { onNavigateToQuestion(it) },
            )
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
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterTopicScreen(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val hasTopic = input.first
  val hasQuestions = input.second
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterTopicScreen(
        Topic.PAYMENTS.takeIf { hasTopic },
        if (hasQuestions) {
          listOf(CLAIMS_Q1, CLAIMS_Q1)
        } else {
          listOf()
        },
        if (hasQuestions) {
          listOf(CLAIMS_Q1, CLAIMS_Q1)
        } else {
          listOf()
        },
        true,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
