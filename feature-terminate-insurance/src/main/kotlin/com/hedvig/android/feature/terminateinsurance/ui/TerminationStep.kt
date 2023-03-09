package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.R
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceViewModel
import com.hedvig.android.feature.terminateinsurance.data.TerminationStep
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.Instant
import java.time.ZoneId

@Composable
internal fun TerminationStepDestination(
  viewModel: TerminateInsuranceViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToSuccessScreen: () -> Unit,
  navigateBack: () -> Unit,
  finishTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val uriHandler = LocalUriHandler.current

  if (uiState.isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    AnimatedContent(targetState = uiState.currentStep) {
      when (it) {
        is TerminationStep.Date -> TerminationDateScreen(
          windowSizeClass = windowSizeClass,
          datePickerState = uiState.datePickerState,
          dateSubmissionSuccess = uiState.dateSubmissionSuccess,
          dateValidator = { long ->
            val selectedDate = toLocalDate(long)
            selectedDate in it.minDate..it.maxDate
          },
          canSubmit = uiState.canContinue,
          submit = viewModel::submitSelectedDate,
          hasError = uiState.dateSubmissionError,
          showedError = viewModel::showedError,
          navigateToSuccessScreen = {
            viewModel.handledSuccess()
            navigateToSuccessScreen()
          },
          navigateBack = navigateBack,
        )

        is TerminationStep.Success -> TerminationInfoScreen(
          windowSizeClass = windowSizeClass,
          navigateBack = finishTerminationFlow,
          title = "",
          headerText = stringResource(hedvig.resources.R.string.TERMINATION_SUCCESSFUL_TITLE),
          bodyText = stringResource(
            hedvig.resources.R.string.TERMINATION_SUCCESSFUL_TEXT,
            it.terminationDate,
            "Hedvig",
          ),
          buttonText = stringResource(hedvig.resources.R.string.TERMINATION_OPEN_SURVEY_LABEL),
          onPrimaryButton = {
            uriHandler.openUri(it.surveyUrl)
          },
          icon = R.drawable.ic_checkmark_in_circle,
        )

        is TerminationStep.Failed -> TerminationInfoScreen(
          windowSizeClass = windowSizeClass,
          navigateBack = finishTerminationFlow,
          title = "",
          headerText = stringResource(hedvig.resources.R.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
          bodyText = it.message ?: stringResource(hedvig.resources.R.string.general_unknown_error),
          onPrimaryButton = finishTerminationFlow,
          icon = R.drawable.ic_warning_triangle,
        )

        null -> TerminationInfoScreen(
          windowSizeClass = windowSizeClass,
          navigateBack = finishTerminationFlow,
          title = "",
          headerText = stringResource(hedvig.resources.R.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
          bodyText = "Could not find next step in flow. Please try again.",
          onPrimaryButton = finishTerminationFlow,
          icon = R.drawable.ic_warning_triangle,
        )
      }
    }
  }
}

private fun toLocalDate(long: Long) =
  Instant.ofEpochMilli(long).atZone(ZoneId.systemDefault()).toLocalDate().toKotlinLocalDate()
