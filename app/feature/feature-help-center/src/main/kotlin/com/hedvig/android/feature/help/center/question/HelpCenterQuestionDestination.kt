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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.halilibo.richtext.commonmark.Markdown
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.feature.help.center.ShowNavigateToInboxViewModel
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionEvent.Reload
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionUiState.Failure
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionUiState.Loading
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionUiState.NoQuestionFound
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionUiState.Success
import com.hedvig.android.feature.help.center.ui.HelpCenterSection
import com.hedvig.android.feature.help.center.ui.StillNeedHelpSection
import hedvig.resources.R

@Composable
internal fun HelpCenterQuestionDestination(
  showNavigateToInboxViewModel: ShowNavigateToInboxViewModel,
  helpCenterQuestionViewModel: HelpCenterQuestionViewModel,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  val uiState by helpCenterQuestionViewModel.uiState.collectAsStateWithLifecycle()
  val showNavigateToInboxButton = showNavigateToInboxViewModel.uiState.collectAsStateWithLifecycle().value
  HelpCenterQuestionScreen(
    uiState = uiState,
    showNavigateToInboxButton = showNavigateToInboxButton,
    onNavigateUp = onNavigateUp,
    onReload = { helpCenterQuestionViewModel.emit(Reload) },
    onNavigateBack = onNavigateBack,
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@Composable
private fun HelpCenterQuestionScreen(
  uiState: HelpCenterQuestionUiState,
  showNavigateToInboxButton: Boolean,
  onNavigateUp: () -> Unit,
  onReload: () -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        title = stringResource(R.string.HC_TITLE),
        onClick = onNavigateUp,
      )
      when (val state = uiState) {
        Failure -> {
          FailureScreen(
            onClick = onReload,
            errorText = stringResource(R.string.GENERAL_ERROR_BODY),
            buttonText = stringResource(R.string.GENERAL_RETRY),
          )
        }

        NoQuestionFound -> {
          FailureScreen(
            onClick = dropUnlessResumed { onNavigateBack() },
            errorText = stringResource(R.string.HC_QUESTION_NOT_FOUND),
            buttonText = stringResource(R.string.general_back_button),
          )
        }

        Loading -> HedvigFullScreenCenterAlignedProgress()
        is Success -> {
          HelpCenterQuestionScreen(
            faqItem = state.faqItem,
            showNavigateToInboxButton = showNavigateToInboxButton,
            onNavigateToInbox = onNavigateToInbox,
            onNavigateToNewConversation = onNavigateToNewConversation,
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
private fun HelpCenterQuestionScreen(
  faqItem: FAQItem,
  showNavigateToInboxButton: Boolean,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  LocalConfiguration.current
  Column(
    Modifier
      .fillMaxSize()
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
            text = faqItem.question,
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
                content = faqItem.answer,
              )
            }
          }
        },
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

@HedvigPreview
@Composable
private fun PreviewHelpCenterQuestionScreen(
  @PreviewParameter(HelpCenterQuestionUiStateProvider::class) uiState: HelpCenterQuestionUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterQuestionScreen(
        uiState = uiState,
        showNavigateToInboxButton = false,
        onNavigateUp = {},
        onReload = {},
        onNavigateBack = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}

private class HelpCenterQuestionUiStateProvider : CollectionPreviewParameterProvider<HelpCenterQuestionUiState>(
  listOf(
    HelpCenterQuestionUiState.Loading,
    HelpCenterQuestionUiState.Failure,
    HelpCenterQuestionUiState.NoQuestionFound,
    HelpCenterQuestionUiState.Success(
      FAQItem("id", "title", "answerrrrrrrr"),
    ),
  ),
)
