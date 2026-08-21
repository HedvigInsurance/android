package com.hedvig.android.feature.onboarding.ui.phone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.onboarding.data.OnboardingRepository
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.phone.SubmissionError.GeneralError
import com.hedvig.android.feature.onboarding.ui.phone.SubmissionError.NumberTooShort
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

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
      currentState = content.copy(isSubmitting = true, showSubmissionError = null)
      onboardingRepository.updateContactInfo(
        email = session.data.email,
        phoneNumber = phoneNumberToSubmit,
      ).fold(
        ifLeft = {
          currentState = (currentState as? OnboardingPhoneUiState.Content ?: content)
            .copy(isSubmitting = false, showSubmissionError = GeneralError)
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
          val content = currentState as? OnboardingPhoneUiState.Content ?: return@CollectEvents
          if (event.phoneNumber.length < 6) {
            currentState = content.copy(showSubmissionError = NumberTooShort())
          } else {
            phoneNumberToSubmit = event.phoneNumber
            submitIteration++
          }
        }

        OnboardingPhoneEvent.DoThisLater -> {
          launch { navigator.continueFrom(OnboardingStepId.PhoneNumber) }
        }

        OnboardingPhoneEvent.ClearSubmissionError -> {
          val content = currentState as? OnboardingPhoneUiState.Content ?: return@CollectEvents
          currentState = content.copy(showSubmissionError = null)
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
    val showSubmissionError: SubmissionError? = null,
  ) : OnboardingPhoneUiState
}

internal sealed interface SubmissionError {
  data class NumberTooShort(val minLength: Int = 6): SubmissionError
  data object GeneralError: SubmissionError
}


internal sealed interface OnboardingPhoneEvent {
  data object Retry : OnboardingPhoneEvent

  data object Close : OnboardingPhoneEvent

  data class Save(val phoneNumber: String) : OnboardingPhoneEvent

  data object DoThisLater : OnboardingPhoneEvent

  data object ClearSubmissionError : OnboardingPhoneEvent
}
