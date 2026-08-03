package com.hedvig.android.feature.onboarding.ui.phone

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.feature.onboarding.data.OnboardingRepository
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_PHONE_SAVE_ERROR
import hedvig.resources.ONBOARDING_PHONE_SUBTITLE
import hedvig.resources.ONBOARDING_PHONE_TITLE
import hedvig.resources.Res
import hedvig.resources.general_save_button
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPhoneViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  onboardingRepository: OnboardingRepository,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingPhoneEvent, OnboardingPhoneUiState>(
    initialState = OnboardingPhoneUiState.Loading,
    presenter = OnboardingPhonePresenter(sessionStore, navigator, onboardingRepository),
  )

internal class OnboardingPhonePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val onboardingRepository: OnboardingRepository,
) : MoleculePresenter<OnboardingPhoneEvent, OnboardingPhoneUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingPhoneEvent>.present(
    lastState: OnboardingPhoneUiState,
  ): OnboardingPhoneUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var phoneNumberToSubmit by remember { mutableStateOf("") }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPhoneUiState.Content) return@LaunchedEffect
      currentState = OnboardingPhoneUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPhoneUiState.Error },
        ifRight = { session ->
          currentState = OnboardingPhoneUiState.Content(
            progress = session.progressFor(OnboardingStepId.PhoneNumber),
            phoneNumber = session.data.phoneNumber.orEmpty(),
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      val content = currentState as? OnboardingPhoneUiState.Content ?: return@LaunchedEffect
      val session = sessionStore.currentSession ?: return@LaunchedEffect
      currentState = content.copy(isSubmitting = true, showSubmissionError = false)
      onboardingRepository.updateContactInfo(
        email = session.data.email,
        phoneNumber = phoneNumberToSubmit,
      ).fold(
        ifLeft = {
          currentState = (currentState as? OnboardingPhoneUiState.Content ?: content)
            .copy(isSubmitting = false, showSubmissionError = true)
        },
        ifRight = {
          // Reset before navigating so returning to this retained entry does not leave Save disabled.
          currentState = content.copy(isSubmitting = false)
          navigator.continueFrom(OnboardingStepId.PhoneNumber)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPhoneEvent.Retry -> {
          loadIteration++
        }

        OnboardingPhoneEvent.Close -> {
          launch { navigator.exitOnboarding() }
        }

        is OnboardingPhoneEvent.Save -> {
          phoneNumberToSubmit = event.phoneNumber
          submitIteration++
        }

        OnboardingPhoneEvent.DoThisLater -> {
          launch { navigator.continueFrom(OnboardingStepId.PhoneNumber) }
        }

        OnboardingPhoneEvent.ClearSubmissionError -> {
          val content = currentState as? OnboardingPhoneUiState.Content ?: return@CollectEvents
          currentState = content.copy(showSubmissionError = false)
        }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingPhoneUiState {
  data object Loading : OnboardingPhoneUiState

  data object Error : OnboardingPhoneUiState

  data class Content(
    val progress: OnboardingProgress,
    val phoneNumber: String,
    val isSubmitting: Boolean = false,
    val showSubmissionError: Boolean = false,
  ) : OnboardingPhoneUiState
}

internal sealed interface OnboardingPhoneEvent {
  data object Retry : OnboardingPhoneEvent

  data object Close : OnboardingPhoneEvent

  data class Save(val phoneNumber: String) : OnboardingPhoneEvent

  data object DoThisLater : OnboardingPhoneEvent

  data object ClearSubmissionError : OnboardingPhoneEvent
}

@Composable
internal fun OnboardingPhoneDestination(viewModel: OnboardingPhoneViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingPhoneUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingPhoneEvent.Close) },
    progressAnimation = viewModel.progressBarAnimation,
  ) {
    when (val content = uiState) {
      OnboardingPhoneUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingPhoneUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingPhoneEvent.Retry) },
        )
      }

      is OnboardingPhoneUiState.Content -> {
        val phoneNumberState = rememberTextFieldState(content.phoneNumber)
        LaunchedEffect(phoneNumberState) {
          snapshotFlow { phoneNumberState.text.toString() }
            .drop(1)
            .collect { viewModel.emit(OnboardingPhoneEvent.ClearSubmissionError) }
        }
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_PHONE_TITLE),
          description = stringResource(Res.string.ONBOARDING_PHONE_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingPhoneKeypad(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        HedvigTextField(
          state = phoneNumberState,
          labelText = stringResource(Res.string.ONBOARDING_PHONE_TITLE),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = if (content.showSubmissionError) {
            HedvigTextFieldDefaults.ErrorState.Error.WithMessage(
              stringResource(Res.string.ONBOARDING_PHONE_SAVE_ERROR),
            )
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
          onPrimaryClick = { viewModel.emit(OnboardingPhoneEvent.Save(phoneNumberState.text.toString())) },
          primaryEnabled = !content.isSubmitting && phoneNumberState.text.isNotBlank(),
          secondaryText = stringResource(Res.string.ONBOARDING_DO_THIS_LATER_BUTTON),
          onSecondaryClick = { viewModel.emit(OnboardingPhoneEvent.DoThisLater) },
        )
      }
    }
  }
}
