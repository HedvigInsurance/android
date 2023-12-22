package com.hedvig.android.feature.help.center.question

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
import androidx.compose.material3.Text
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
import com.hedvig.android.core.designsystem.material3.onPinkContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.pinkContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.HelpCenterSectionWithClickableRows
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun HelpCenterQuestionDestination(
  questionId: String,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
  openChat: () -> Unit,
) {
  Text("HelpCenterDestinations.Question:$questionId")
  val question = Question.entries.find { it.questionId == questionId }
  val relatedQuestions = question
    ?.relatedQuestionIds
    ?.mapNotNull { id ->
      Question.entries.find { it.questionId == id }
    }
    ?.toPersistentList() ?: persistentListOf()
  HelpCenterQuestionScreen(
    question = question,
    relatedQuestions = relatedQuestions,
    onNavigateToQuestion = onNavigateToQuestion,
    onNavigateUp = onNavigateUp,
    onNavigateBack = onNavigateBack,
    openChat = openChat,
  )
}

@Composable
private fun HelpCenterQuestionScreen(
  question: Question?,
  relatedQuestions: ImmutableList<Question>,
  onNavigateToQuestion: (questionId: String) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
  openChat: () -> Unit,
) {
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = question?.let { stringResource(id = question.titleRes) } ?: "",
        onClick = onNavigateUp,
      )
      if (question == null) {
        // todo help-center: Add some sort of fallback screen to explain that this question is not found
        HedvigErrorSection(
          retry = onNavigateBack,
          // todo help-center: localize
          title = "Question not found",
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
          HelpCenterSection(
            // todo help-center: localize
            title = "Question",
            chipContainerColor = MaterialTheme.colorScheme.infoContainer,
            contentColor = MaterialTheme.colorScheme.onInfoContainer,
            content = {
              Text(
                text = stringResource(question.questionRes),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                  .padding(horizontal = 16.dp)
                  .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
              )
            },
          )
          Spacer(Modifier.height(32.dp))
          HelpCenterSection(
            // todo help-center: localize
            title = "Answer",
            chipContainerColor = MaterialTheme.colorScheme.typeContainer,
            contentColor = MaterialTheme.colorScheme.onTypeContainer,
            content = {
              Text(
                // todo help-center: here do some shenanigans for clickable links. This can be used for inspiration:
                //  https://github.com/HedvigInsurance/android/blob/5156dbc7f62f0d807402113c749ff3d378dfbd5e/app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/ui/TextWithClickableUrls.kt#L24
                text = stringResource(question.answerRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                  .padding(horizontal = 16.dp)
                  .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
              )
            },
          )
          if (relatedQuestions.isNotEmpty()) {
            Spacer(Modifier.height(96.dp))
            HelpCenterSectionWithClickableRows(
              title = "Related questions",
              chipContainerColor = MaterialTheme.colorScheme.pinkContainer,
              contentColor = MaterialTheme.colorScheme.onPinkContainer,
              items = relatedQuestions,
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
private fun PreviewHelpCenterQuestionScreen(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val hasQuestion = input.first
  val hasRelatedQuestions = input.second
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterQuestionScreen(
        Question.WhenIsInsuranceCharged.takeIf { hasQuestion },
        if (hasRelatedQuestions) {
          persistentListOf(
            Question.WhenIsInsuranceCharged,
            Question.WhenIsInsuranceActivated,
            Question.HowToMakeClaim,
          )
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
