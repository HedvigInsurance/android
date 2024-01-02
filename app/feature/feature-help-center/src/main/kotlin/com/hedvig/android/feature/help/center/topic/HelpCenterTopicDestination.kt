package com.hedvig.android.feature.help.center.topic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onPurpleContainer
import com.hedvig.android.core.designsystem.material3.purpleContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun HelpCenterTopicDestination(
  topicId: String,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
  openChat: () -> Unit,
) {
  val topic = Topic.entries.find { it.topicId == topicId }
  val commonQuestions = topic
    ?.commonQuestionIds
    ?.mapNotNull { questionId ->
      Question.entries.find { it.questionId == questionId }
    }
    ?.toPersistentList() ?: persistentListOf()
  val allQuestions = topic
    ?.allQuestionIds
    ?.mapNotNull { questionId ->
      Question.entries.find { it.questionId == questionId }
    }
    ?.toPersistentList() ?: persistentListOf()
  HelpCenterTopicScreen(
    topic = topic,
    commonQuestions = commonQuestions,
    allQuestions = allQuestions,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateUp = onNavigateUp,
    onNavigateBack = onNavigateBack,
    openChat = openChat,
  )
}

@Composable
private fun HelpCenterTopicScreen(
  topic: Topic?,
  commonQuestions: ImmutableList<Question>,
  allQuestions: ImmutableList<Question>,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
  openChat: () -> Unit,
) {
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = topic?.titleRes?.let { stringResource(it) } ?: stringResource(id = R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      if (topic == null) {
        HedvigErrorSection(
          retry = onNavigateBack,
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
          retry = onNavigateBack,
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
              title = stringResource(id = R.string.HC_COMMON_QUESTIONS_TITLE),
              chipContainerColor = MaterialTheme.colorScheme.infoContainer,
              contentColor = MaterialTheme.colorScheme.onInfoContainer,
              items = commonQuestions,
              itemText = { resources.getString(it.questionRes) },
              onClickItem = { onNavigateToQuestion(it.questionId) },
            )
          }
          if (commonQuestions.isNotEmpty() && allQuestions.isNotEmpty()) {
            Spacer(Modifier.height(40.dp))
          }
          if (allQuestions.isNotEmpty()) {
            HelpCenterSectionWithClickableRows(
              title = stringResource(id = R.string.HC_ALL_QUESTION_TITLE),
              chipContainerColor = MaterialTheme.colorScheme.purpleContainer,
              contentColor = MaterialTheme.colorScheme.onPurpleContainer,
              items = allQuestions,
              itemText = { resources.getString(it.questionRes) },
              onClickItem = { onNavigateToQuestion(it.questionId) },
            )
          }
          Spacer(Modifier.weight(1f))
          Spacer(Modifier.height(16.dp))
          StillNeedHelpSection(openChat)
          Spacer(Modifier.height(56.dp))
          Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
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
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterTopicScreen(
        Topic.Payments.takeIf { hasTopic },
        if (hasQuestions) {
          persistentListOf(Question.WhenIsInsuranceCharged, Question.WhenIsInsuranceCharged)
        } else {
          persistentListOf()
        },
        if (hasQuestions) {
          persistentListOf(Question.WhenIsInsuranceCharged, Question.WhenIsInsuranceCharged)
        } else {
          persistentListOf()
        },
        {},
        {},
        {},
        {},
      )
    }
  }
}
