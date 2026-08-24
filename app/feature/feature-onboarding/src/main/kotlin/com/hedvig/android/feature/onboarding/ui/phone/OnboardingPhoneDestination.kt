package com.hedvig.android.feature.onboarding.ui.phone

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.phone.SubmissionError.GeneralError
import com.hedvig.android.feature.onboarding.ui.phone.SubmissionError.NumberTooShort
import hedvig.resources.CLAIM_CHAT_FORM_TEXT_MIN_CHAR
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_PHONE_SAVE_ERROR
import hedvig.resources.ONBOARDING_PHONE_SUBTITLE
import hedvig.resources.ONBOARDING_PHONE_TITLE
import hedvig.resources.Res
import hedvig.resources.general_save_button
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingPhoneDestination(viewModel: OnboardingPhoneViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingPhoneScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingPhoneEvent.Close) },
    onRetry = { viewModel.emit(OnboardingPhoneEvent.Retry) },
    onSave = { viewModel.emit(OnboardingPhoneEvent.Save(it)) },
    onClearSubmissionError = { viewModel.emit(OnboardingPhoneEvent.ClearSubmissionError) },
    onDoThisLater = { viewModel.emit(OnboardingPhoneEvent.DoThisLater) },
  )
}

@Composable
private fun OnboardingPhoneScreen(
  uiState: OnboardingPhoneUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onSave: (phoneNumber: String) -> Unit,
  onClearSubmissionError: () -> Unit,
  onDoThisLater: () -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingPhoneUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (val content = uiState) {
      OnboardingPhoneUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingPhoneUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingPhoneUiState.Content -> {
        val phoneNumberState = rememberTextFieldState(content.phoneNumber)
        LaunchedEffect(phoneNumberState) {
          snapshotFlow { phoneNumberState.text.toString() }
            .drop(1)
            .collect { onClearSubmissionError() }
        }
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_PHONE_TITLE),
          description = stringResource(Res.string.ONBOARDING_PHONE_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingPhoneKeypad(
          onKeypadClick = { label ->
            phoneNumberState.edit {
              append(label)
              placeCursorAtEnd()
            }
          },
          modifier = Modifier.align(Alignment.CenterHorizontally).semantics {
            hideFromAccessibility()
          },
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        HedvigTextField(
          state = phoneNumberState,
          labelText = stringResource(Res.string.ONBOARDING_PHONE_TITLE),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = if (content.showSubmissionError != null) {
            val text = when (content.showSubmissionError) {
              GeneralError -> stringResource(Res.string.ONBOARDING_PHONE_SAVE_ERROR)

              NumberTooShort -> stringResource(
                Res.string.CLAIM_CHAT_FORM_TEXT_MIN_CHAR,
                MINIMUM_PHONE_NUMBER_LENGTH,
              )
            }
            HedvigTextFieldDefaults.ErrorState.Error.WithMessage(text)
          } else {
            HedvigTextFieldDefaults.ErrorState.NoError
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.general_save_button),
          onPrimaryClick = { onSave(phoneNumberState.text.toString()) },
          primaryEnabled = !content.isSubmitting && phoneNumberState.text.isNotBlank(),
          secondaryText = stringResource(Res.string.ONBOARDING_DO_THIS_LATER_BUTTON),
          onSecondaryClick = onDoThisLater,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingPhoneScreen(
  @PreviewParameter(OnboardingPhoneUiStateProvider::class) uiState: OnboardingPhoneUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingPhoneScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onSave = {},
        onClearSubmissionError = {},
        onDoThisLater = {},
      )
    }
  }
}

private class OnboardingPhoneUiStateProvider : CollectionPreviewParameterProvider<OnboardingPhoneUiState>(
  listOf(
    OnboardingPhoneUiState.Loading,
    OnboardingPhoneUiState.Error,
    OnboardingPhoneUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      phoneNumber = "070 123 45 67",
    ),
    OnboardingPhoneUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      phoneNumber = "070 123 45 67",
      showSubmissionError = NumberTooShort,
    ),
  ),
)
