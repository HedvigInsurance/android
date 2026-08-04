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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.onboarding.data.OnboardingPayinStatus
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
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_FOOTNOTE
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_SUBTITLE
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_SWITCH_ACCOUNTS_LATER
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_TITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPaymentViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
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
            payinStatus = session.data.payinStatus,
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
              payinStatus = refreshed.data.payinStatus,
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
    val payinStatus: OnboardingPayinStatus,
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

  OnboardingPaymentScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingPaymentEvent.Close) },
    onRetry = { viewModel.emit(OnboardingPaymentEvent.Retry) },
    onConnectPayment = { viewModel.emit(OnboardingPaymentEvent.ConnectPayment) },
    onContinue = { viewModel.emit(OnboardingPaymentEvent.Continue) },
  )
}

@Composable
private fun OnboardingPaymentScreen(
  uiState: OnboardingPaymentUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onConnectPayment: () -> Unit,
  onContinue: () -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingPaymentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (val content = uiState) {
      OnboardingPaymentUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingPaymentUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingPaymentUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        when (content.payinStatus) {
          OnboardingPayinStatus.NeedsSetup -> {
            OnboardingStepHeader(
              title = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_TITLE),
              description = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_SUBTITLE),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            HedvigText(
              text = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_FOOTNOTE),
              style = HedvigTheme.typography.finePrint,
              color = HedvigTheme.colorScheme.textSecondary,
              modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp),
            )
            OnboardingStepButtons(
              primaryText = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_TITLE),
              onPrimaryClick = onConnectPayment,
            )
          }

          // A payin method exists (active, or pending bank activation). The design's connected
          // state is just the "switch accounts later" hint plus Continue; we deliberately make no
          // claim about the connection being active, so pending and active render the same.
          OnboardingPayinStatus.Pending, OnboardingPayinStatus.Active -> {
            OnboardingStepHeader(title = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_TITLE))
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            HedvigText(
              text = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_SWITCH_ACCOUNTS_LATER),
              style = HedvigTheme.typography.finePrint,
              color = HedvigTheme.colorScheme.textSecondary,
              modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            OnboardingStepButtons(
              primaryText = stringResource(Res.string.general_continue_button),
              onPrimaryClick = onContinue,
            )
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingPaymentScreen(
  @PreviewParameter(OnboardingPaymentUiStateProvider::class) uiState: OnboardingPaymentUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingPaymentScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onConnectPayment = {},
        onContinue = {},
      )
    }
  }
}

private class OnboardingPaymentUiStateProvider : CollectionPreviewParameterProvider<OnboardingPaymentUiState>(
  listOf(
    OnboardingPaymentUiState.Loading,
    OnboardingPaymentUiState.Error,
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.NeedsSetup,
    ),
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.Pending,
    ),
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.Active,
    ),
  ),
)
