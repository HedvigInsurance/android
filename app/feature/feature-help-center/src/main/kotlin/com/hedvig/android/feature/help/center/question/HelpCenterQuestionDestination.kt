package com.hedvig.android.feature.help.center.question

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
import com.halilibo.richtext.commonmark.Markdown
import com.hedvig.android.compose.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R

@Composable
internal fun HelpCenterQuestionDestination(
  showNavigateToInboxViewModel: ShowNavigateToInboxViewModel,
  questionId: Question,
  onNavigateToQuestion: (questionId: Question) -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  val question = Question.entries.find { it == questionId }
  val relatedQuestions = question
    ?.relatedQuestionIds
    ?.mapNotNull { id ->
      Question.entries.firstOrNull { it == id }
    } ?: listOf()
  HelpCenterQuestionScreen(
    question = question,
    relatedQuestions = relatedQuestions,
    showNavigateToInboxButton = showNavigateToInboxViewModel.uiState.collectAsStateWithLifecycle().value,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateUp = onNavigateUp,
    onNavigateBack = onNavigateBack,
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@Composable
private fun HelpCenterQuestionScreen(
  question: Question?,
  relatedQuestions: List<Question>,
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
        title = question?.let { stringResource(id = question.titleRes) } ?: stringResource(R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      if (question == null) {
        HedvigErrorSection(
          onButtonClick = onNavigateBack,
          title = stringResource(R.string.HC_QUESTION_NOT_FOUND),
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
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        ) {
          Column(
            modifier =
              Modifier.padding(
                WindowInsets
                  .safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues() + PaddingValues(horizontal = 16.dp),
              ),
          ) {
            Spacer(Modifier.height(16.dp))
            HelpCenterSection(
              title = stringResource(id = R.string.HC_QUESTION_TITLE),
              chipContainerColor = HighlightColor.Blue(LIGHT),
              content = {
                HedvigText(
                  text = stringResource(question.questionRes),
                  style = HedvigTheme.typography.bodySmall,
                )
              },
            )
            Spacer(Modifier.height(32.dp))
            HelpCenterSection(
              title = stringResource(R.string.HC_ANSWER_TITLE),
              chipContainerColor = HighlightColor.Green(LIGHT),
              content = {
                ProvideTextStyle(
                  HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textSecondary),
                ) {
                  RichText {
                    Markdown(
                      content = stringResource(id = question.answerRes),
                    )
                  }
                }
              },
            )
            if (relatedQuestions.isNotEmpty()) {
              Spacer(Modifier.height(96.dp))
              HelpCenterSectionWithClickableRows(
                title = "Related questions",
                chipContainerColor = HighlightColor.Pink(LIGHT),
                items = relatedQuestions,
                itemText = { resources.getString(it.questionRes) },
                onClickItem = { onNavigateToQuestion(it) },
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
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterQuestionScreen(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val hasQuestion = input.first
  val hasRelatedQuestions = input.second
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterQuestionScreen(
        question = Question.CLAIMS_Q1.takeIf { hasQuestion },
        relatedQuestions = if (hasRelatedQuestions) {
          listOf(
            Question.CLAIMS_Q1,
          )
        } else {
          listOf()
        },
        showNavigateToInboxButton = true,
        onNavigateToQuestion = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        onNavigateUp = {},
        onNavigateBack = {},
      )
    }
  }
}
