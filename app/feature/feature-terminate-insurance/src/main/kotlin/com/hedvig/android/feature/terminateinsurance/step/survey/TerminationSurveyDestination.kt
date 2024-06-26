package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextFieldDefaults
import com.hedvig.android.core.designsystem.material3.alwaysBlackContainer
import com.hedvig.android.core.designsystem.material3.onAlwaysBlackContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Campaign
import com.hedvig.android.core.ui.infocard.InfoCardTextButton
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun TerminationSurveyDestination(
  viewModel: TerminationSurveyViewModel,
  navigateUp: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep) -> Unit,
  navigateToSubOptions: ((List<TerminationSurveyOption>) -> Unit)?,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.nextNavigationStep) {
    val nextStep = uiState.nextNavigationStep
    if (nextStep != null) {
      when (nextStep) {
        is SurveyNavigationStep.NavigateToNextTerminationStep -> {
          viewModel.emit(TerminationSurveyEvent.ClearNextStep)
          navigateToNextStep(nextStep.step)
        }

        SurveyNavigationStep.NavigateToSubOptions -> {
          viewModel.emit(TerminationSurveyEvent.ClearNextStep)
          uiState.selectedOption?.let {
            navigateToSubOptions?.invoke(it.subOptions)
          }
        }
      }
    }
  }
  TerminationSurveyScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToMovingFlow = navigateToMovingFlow,
    closeTerminationFlow = closeTerminationFlow,
    onContinueClick = { viewModel.emit(TerminationSurveyEvent.Continue) },
    selectOption = { option ->
      viewModel.emit(TerminationSurveyEvent.SelectOption(option))
    },
    changeFeedbackForSelectedReason = { feedback ->
      viewModel.emit(TerminationSurveyEvent.ChangeFeedbackForSelectedReason(feedback))
    },
    onCloseFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.CloseFullScreenEditText)
    },
    onLaunchFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.ShowFullScreenEditText(it))
    },
    openUrl = openUrl,
  )
}

@Composable
private fun TerminationSurveyScreen(
  uiState: TerminationSurveyState,
  selectOption: (TerminationSurveyOption) -> Unit,
  navigateUp: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  onCloseFullScreenEditText: () -> Unit,
  onLaunchFullScreenEditText: (option: TerminationSurveyOption) -> Unit,
  changeFeedbackForSelectedReason: (feedback: String?) -> Unit,
  onContinueClick: () -> Unit,
) {
  Box {
    TerminationScaffold(
      navigateUp = navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    ) {
      Text(
        style = MaterialTheme.typography.headlineSmall.copy(
          lineBreak = LineBreak.Heading,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        text = stringResource(id = R.string.TERMINATION_SURVEY_SUBTITLE),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      AnimatedVisibility(
        visible = uiState.errorWhileLoadingNextStep,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Column {
          WarningTextWithIcon(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth()
              .wrapContentWidth(),
            text = stringResource(R.string.something_went_wrong),
          )
          Spacer(Modifier.height(16.dp))
        }
      }
      for (reason in uiState.reasons) {
        Column {
          RadioOption(
            optionText = reason.surveyOption.title,
            chosenState = if (uiState.selectedOption == reason.surveyOption) Chosen else NotChosen,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            onClick = { selectOption(reason.surveyOption) },
            radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
          )

          Spacer(modifier = (Modifier.height(4.dp)))
          AnimatedVisibility(visible = reason.surveyOption == uiState.selectedOption) {
            Column {
              val suggestion = reason.surveyOption.suggestion
              if (suggestion != null) {
                val text = when (suggestion) {
                  SurveyOptionSuggestion.Action.UpdateAddress -> stringResource(
                    id = R.string.TERMINATION_SURVEY_MOVING_SUGGESTION,
                  )

                  is SurveyOptionSuggestion.Redirect -> suggestion.description
                }
                val buttonText = when (suggestion) {
                  SurveyOptionSuggestion.Action.UpdateAddress -> stringResource(
                    R.string.TERMINATION_SURVEY_MOVING_BUTTON,
                  )

                  is SurveyOptionSuggestion.Redirect -> suggestion.buttonTitle
                }
                val onSuggestionButtonClick: () -> Unit = when (suggestion) {
                  SurveyOptionSuggestion.Action.UpdateAddress -> { -> navigateToMovingFlow() }

                  is SurveyOptionSuggestion.Redirect -> { -> openUrl(suggestion.url) }
                }
                VectorInfoCard(
                  text = text,
                  icon = Icons.Hedvig.Campaign,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                  iconColor = MaterialTheme.colorScheme.typeElement,
                  colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.typeContainer,
                    contentColor = MaterialTheme.colorScheme.onTypeContainer,
                  ),
                ) {
                  Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                  ) {
                    InfoCardTextButton(
                      text = buttonText,
                      onClick = onSuggestionButtonClick,
                      modifier = Modifier.weight(1f),
                    )
                  }
                }
                Spacer(modifier = (Modifier.height(4.dp)))
              }
              if (reason.surveyOption.feedBackRequired) {
                val feedback = reason.feedBack
                HedvigCard(
                  onClick = {
                    onLaunchFullScreenEditText(reason.surveyOption)
                  },
                  colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(100.dp)
                    .padding(horizontal = 16.dp),
                ) {
                  Column {
                    Row(
                      verticalAlignment = Alignment.Top,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                      Text(
                        overflow = TextOverflow.Ellipsis,
                        text = feedback ?: stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (feedback != null) {
                          MaterialTheme.colorScheme.secondary
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f),
                      )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
                Spacer(modifier = (Modifier.height(4.dp)))
              }
            }
          }
        }
      }
      Spacer(Modifier.height(12.dp))
      HedvigContainedButton(
        stringResource(id = R.string.general_continue_button),
        enabled = uiState.continueAllowed,
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
          disabledContainerColor = MaterialTheme.colorScheme.surface,
          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = onContinueClick,
        isLoading = uiState.navigationStepLoadingForReason != null,
      )
      Spacer(Modifier.height(16.dp))
    }

    FeedbackEditableText(uiState, onCloseFullScreenEditText, changeFeedbackForSelectedReason)
  }
}

@Composable
private fun FeedbackEditableText(
  uiState: TerminationSurveyState,
  onCloseFullScreenEditText: () -> Unit,
  changeFeedbackForSelectedReason: (feedback: String?) -> Unit,
) {
  val showFullScreenEditTextTransition = updateTransition(
    uiState.showFullScreenEditText,
    "showFullScreenEditTextTransition",
  )
  showFullScreenEditTextTransition.AnimatedContent(
    transitionSpec = {
      fadeIn() togetherWith fadeOut()
    },
    modifier = Modifier.fillMaxSize(),
  ) { showFullScreenEditText: TerminationReason? ->
    val reason = showFullScreenEditText
    Box(Modifier.fillMaxSize()) {
      if (reason != null) {
        HedvigTheme(darkTheme = true) {
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
          ) {
            FeedbackEditableText(
              reason.feedBack,
              modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                  WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical),
                )
                .animateEnterExit(
                  enter = slideInVertically(initialOffsetY = { it / 2 }),
                  exit = ExitTransition.None,
                ),
              onCancelClick = onCloseFullScreenEditText,
              onSaveClick = { newFeedback ->
                changeFeedbackForSelectedReason(newFeedback)
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FeedbackEditableText(
  feedbackText: String?,
  onSaveClick: (String?) -> Unit,
  onCancelClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }
  val hint = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT)
  var textValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = feedbackText ?: "",
        selection = TextRange((feedbackText ?: "").length),
      ),
    )
  }
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  BackHandler(true) {
    focusManager.clearFocus()
    onCancelClick()
  }
  Column(
    modifier
      .fillMaxSize()
      .imePadding()
      .padding(8.dp),
  ) {
    BasicTextField(
      value = textValue,
      onValueChange = {
        textValue = it.ofMaxLength(2000)
      },
      cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
      modifier = Modifier
        .weight(1f)
        .focusRequester(focusRequester)
        .background(
          MaterialTheme.colorScheme.surface,
          shape = HedvigTextFieldDefaults.shape,
        ),
      textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
      decorationBox = @Composable { innerTextField ->
        Column {
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f),
          ) {
            HedvigTextFieldDefaults.DecorationBox(
              value = textValue.text,
              colors = HedvigTextFieldDefaults.colors(
                typingHighlightColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
              ),
              placeholder = {
                Text(
                  text = hint,
                  Modifier.fillMaxSize(),
                  fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                )
              },
              innerTextField = innerTextField,
              enabled = true,
              singleLine = false,
              interactionSource = remember { MutableInteractionSource() },
              visualTransformation = VisualTransformation.None,
            )
          }
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
          ) {
            Text(
              text = "${textValue.text.length}/2000",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
    ) {
      HedvigContainedSmallButton(
        text = stringResource(id = R.string.general_cancel_button),
        onClick = {
          focusManager.clearFocus()
          onCancelClick()
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
      )
      Spacer(modifier = Modifier.width(8.dp))
      HedvigContainedSmallButton(
        text = stringResource(id = R.string.general_save_button),
        onClick = {
          focusManager.clearFocus()
          val valueToSave = textValue.text.ifEmpty { null }
          onSaveClick(valueToSave)
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.onAlwaysBlackContainer,
          contentColor = MaterialTheme.colorScheme.alwaysBlackContainer,
        ),
      )
    }
  }
}

fun TextFieldValue.ofMaxLength(maxLength: Int): TextFieldValue {
  val overLength = text.length - maxLength
  return if (overLength > 0) {
    val headIndex = selection.end - overLength
    val trailIndex = selection.end
    if (headIndex >= 0) {
      copy(
        text = text.substring(0, headIndex) + text.substring(trailIndex, text.length),
        selection = TextRange(headIndex),
      )
    } else {
      copy(text.take(maxLength), selection = TextRange(maxLength))
    }
  } else {
    this
  }
}

@HedvigPreview
@Composable
private fun ShowSurveyScreenPreview(
  @PreviewParameter(
    ShowSurveyUiStateProvider::class,
  ) uiState: TerminationSurveyState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSurveyScreen(
        uiState = uiState,
        selectOption = {},
        navigateUp = {},
        navigateToMovingFlow = {},
        closeTerminationFlow = {},
        changeFeedbackForSelectedReason = { String ->
        },
        onContinueClick = {},
        onCloseFullScreenEditText = {},
        onLaunchFullScreenEditText = {},
        openUrl = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun FeedbackEditableTextPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FeedbackEditableText(
        null,
        {},
        {},
        Modifier.fillMaxSize(),
      )
    }
  }
}

private class ShowSurveyUiStateProvider :
  CollectionPreviewParameterProvider<TerminationSurveyState>(
    listOf(
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        selectedOption = previewReason1.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        selectedOption = previewReason3.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        errorWhileLoadingNextStep = true,
        selectedOption = previewReason2.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
    ),
  )

private val previewReason1 = TerminationReason(
  TerminationSurveyOption(
    id = "1",
    title = "I'm moving",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "11",
        title = "I'm moving in with someone else",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
        listIndex = 0,
      ),
      TerminationSurveyOption(
        id = "12",
        title = "I'm moving abroad",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
        listIndex = 1,
      ),
      TerminationSurveyOption(
        id = "23",
        title = "Other",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
        listIndex = 2,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Action.UpdateAddress,
    feedBackRequired = true,
    listIndex = 0,
  ),
  null,
)

private val previewReason2 = TerminationReason(
  TerminationSurveyOption(
    id = "2",
    title = "I got a better offer elsewhere",
    subOptions = listOf(),
    suggestion = null,
    feedBackRequired = true,
    listIndex = 1,
  ),
  feedBack = LoremIpsum(25).values.first(),
)

private val previewReason3 = TerminationReason(
  TerminationSurveyOption(
    id = "3",
    title = "I am dissatisfied",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "31",
        title = "I am dissatisfied with the coverage",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
        listIndex = 0,
      ),
      TerminationSurveyOption(
        id = "32",
        title = "I am dissatisfied with the service",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
        listIndex = 1,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Redirect(
      "http://www.google.com",
      "Do this action instead",
      "Click here to do it",
    ),
    feedBackRequired = false,
    listIndex = 3,
  ),
  null,
)
