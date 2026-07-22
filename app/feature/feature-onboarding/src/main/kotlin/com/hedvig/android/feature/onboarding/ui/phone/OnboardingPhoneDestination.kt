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
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPhoneViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  onboardingRepository: OnboardingRepository,
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
          // TODO: Add "Phone number" / "Telefonnummer" to Lokalise
          title = "Phone number",
          // TODO: Add "Add your phone number so we can reach you if something happens" /
          //  "Lägg till ditt telefonnummer så att vi kan nå dig om något händer" to Lokalise
          description = "Add your phone number so we can reach you if something happens",
        )
        Spacer(Modifier.weight(1f))
        HedvigTextField(
          state = phoneNumberState,
          // TODO: Add "Phone number" / "Telefonnummer" to Lokalise
          labelText = "Phone number",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          errorState = if (content.showSubmissionError) {
            // TODO: Add "Could not save, please try again" / "Kunde inte spara, försök igen" to Lokalise
            HedvigTextFieldDefaults.ErrorState.Error.WithMessage("Could not save, please try again")
          } else {
            HedvigTextFieldDefaults.ErrorState.NoError
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        OnboardingStepButtons(
          // TODO: Add "Save" / "Spara" to Lokalise
          primaryText = "Save",
          onPrimaryClick = { viewModel.emit(OnboardingPhoneEvent.Save(phoneNumberState.text.toString())) },
          primaryEnabled = !content.isSubmitting && phoneNumberState.text.isNotBlank(),
          // TODO: Add "Do this later" / "Gör det senare" to Lokalise
          secondaryText = "Do this later",
          onSecondaryClick = { viewModel.emit(OnboardingPhoneEvent.DoThisLater) },
        )
      }
    }
  }
}
