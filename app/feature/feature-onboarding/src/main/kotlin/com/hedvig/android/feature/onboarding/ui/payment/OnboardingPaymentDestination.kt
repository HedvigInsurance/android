package com.hedvig.android.feature.onboarding.ui.payment

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
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
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPaymentViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
) : MoleculeViewModel<OnboardingPaymentEvent, OnboardingPaymentUiState>(
    initialState = OnboardingPaymentUiState.Loading,
    presenter = OnboardingPaymentPresenter(sessionStore, navigator),
  )

internal class OnboardingPaymentPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingPaymentEvent, OnboardingPaymentUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingPaymentEvent>.present(
    lastState: OnboardingPaymentUiState,
  ): OnboardingPaymentUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPaymentUiState.Content) return@LaunchedEffect
      currentState = OnboardingPaymentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPaymentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingPaymentUiState.Content(
            progress = session.progressFor(OnboardingStepId.ConnectPayment),
            isConnected = session.data.hasConnectedPayinMethod,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPaymentEvent.Retry -> loadIteration++

        OnboardingPaymentEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingPaymentEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed ->
            currentState = OnboardingPaymentUiState.Content(
              progress = refreshed.progressFor(OnboardingStepId.ConnectPayment),
              isConnected = refreshed.data.hasConnectedPayinMethod,
            )
          }
        }

        OnboardingPaymentEvent.ConnectPayment -> navigator.openConnectPayment()

        OnboardingPaymentEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.ConnectPayment) }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingPaymentUiState {
  data object Loading : OnboardingPaymentUiState

  data object Error : OnboardingPaymentUiState

  data class Content(
    val progress: OnboardingProgress,
    val isConnected: Boolean,
  ) : OnboardingPaymentUiState
}

internal sealed interface OnboardingPaymentEvent {
  data object Retry : OnboardingPaymentEvent

  data object Close : OnboardingPaymentEvent

  data object Refresh : OnboardingPaymentEvent

  data object ConnectPayment : OnboardingPaymentEvent

  data object Continue : OnboardingPaymentEvent
}

@Composable
internal fun OnboardingPaymentDestination(viewModel: OnboardingPaymentViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(OnboardingPaymentEvent.Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }

  OnboardingStepScaffold(
    progress = (uiState as? OnboardingPaymentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingPaymentEvent.Close) },
  ) {
    when (val content = uiState) {
      OnboardingPaymentUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingPaymentUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingPaymentEvent.Retry) },
        )
      }

      is OnboardingPaymentUiState.Content -> {
        Spacer(Modifier.height(8.dp))
        if (!content.isConnected) {
          OnboardingStepHeader(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            title = "Connect payment",
            // TODO: Add "Add a payment method to activate your insurance" /
            //  "Lägg till en betalmetod för att aktivera din försäkring" to Lokalise
            description = "Add a payment method to activate your insurance",
          )
          Spacer(Modifier.weight(1f))
          // TODO: Add "Adding a payment method is required to keep your insurance active" /
          //  "Du behöver lägga till en betalmetod för att hålla din försäkring aktiv" to Lokalise
          HedvigText(
            text = "Adding a payment method is required to keep your insurance active",
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .padding(horizontal = 32.dp),
          )
          OnboardingStepButtons(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            primaryText = "Connect payment",
            onPrimaryClick = { viewModel.emit(OnboardingPaymentEvent.ConnectPayment) },
          )
        } else {
          OnboardingStepHeader(
            // TODO: Add "Connect payment" / "Koppla betalning" to Lokalise
            title = "Connect payment",
            // TODO: Add "Your payment method is connected" / "Din betalmetod är kopplad" to Lokalise
            description = "Your payment method is connected",
          )
          Spacer(Modifier.weight(1f))
          Icon(
            imageVector = HedvigIcons.Checkmark,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          Spacer(Modifier.height(16.dp))
          // TODO: Add "You can switch accounts later in settings" /
          //  "Du kan byta konto senare i inställningar" to Lokalise
          HedvigText(
            text = "You can switch accounts later in settings",
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          OnboardingStepButtons(
            // TODO: Add "Continue" / "Fortsätt" to Lokalise
            primaryText = "Continue",
            onPrimaryClick = { viewModel.emit(OnboardingPaymentEvent.Continue) },
          )
        }
      }
    }
  }
}
