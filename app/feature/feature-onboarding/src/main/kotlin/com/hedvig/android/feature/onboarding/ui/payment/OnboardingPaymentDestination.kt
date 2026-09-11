package com.hedvig.android.feature.onboarding.ui.payment

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
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
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.onboarding.data.OnboardingPayinProvider
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
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_BANK_LABEL
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_SUBTITLE
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_SWITCH_ACCOUNTS_LATER
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_TITLE
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.PAYMENT_CONNECT_TITLE
import hedvig.resources.PAYMENT_OPTION_SWISH_SUBTITLE
import hedvig.resources.PAYMENT_OPTION_TRUSTLY_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import hedvig.resources.swish
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
    var hasAttemptedToConnect by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<OnboardingPayinProvider?>(null) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPaymentUiState.Content) return@LaunchedEffect
      currentState = OnboardingPaymentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPaymentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingPaymentUiState.Content(
            progress = session.progressFor(OnboardingStepId.ConnectPayment),
            payinStatus = session.data.payinStatus,
            availableProviders = session.data.availablePayinProviders,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPaymentEvent.Retry -> {
          loadIteration++
        }

        OnboardingPaymentEvent.Close -> {
          launch { navigator.exitOnboarding() }
        }

        OnboardingPaymentEvent.Refresh -> {
          launch {
            sessionStore.refreshData().onRight { refreshed ->
              currentState = OnboardingPaymentUiState.Content(
                progress = refreshed.progressFor(OnboardingStepId.ConnectPayment),
                payinStatus = refreshed.data.payinStatus,
                availableProviders = refreshed.data.availablePayinProviders,
              )
            }
          }
        }

        is OnboardingPaymentEvent.SelectProvider -> {
          selectedProvider = event.provider
        }

        OnboardingPaymentEvent.ConnectPayment -> {
          val provider = selectedProvider
          if (provider != null) {
            hasAttemptedToConnect = true
            navigator.openPayinSetup(provider)
          }
        }

        OnboardingPaymentEvent.Continue -> {
          launch { navigator.continueFrom(OnboardingStepId.ConnectPayment) }
        }
      }
    }

    return when (val state = currentState) {
      is OnboardingPaymentUiState.Content -> state.copy(
        hasAttemptedToConnect = hasAttemptedToConnect,
        selectedProvider = selectedProvider,
      )

      else -> state
    }
  }
}

internal sealed interface OnboardingPaymentUiState {
  data object Loading : OnboardingPaymentUiState

  data object Error : OnboardingPaymentUiState

  data class Content(
    val progress: OnboardingProgress,
    val payinStatus: OnboardingPayinStatus,
    val availableProviders: List<OnboardingPayinProvider>,
    val selectedProvider: OnboardingPayinProvider? = null,
    // The skip is offered only once the member has entered the connect flow at least once, so a
    // member who tries and does not finish is never stuck on this step.
    val hasAttemptedToConnect: Boolean = false,
  ) : OnboardingPaymentUiState
}

internal sealed interface OnboardingPaymentEvent {
  data object Retry : OnboardingPaymentEvent

  data object Close : OnboardingPaymentEvent

  data object Refresh : OnboardingPaymentEvent

  data class SelectProvider(val provider: OnboardingPayinProvider) : OnboardingPaymentEvent

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
    onProviderSelected = { viewModel.emit(OnboardingPaymentEvent.SelectProvider(it)) },
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
  onProviderSelected: (OnboardingPayinProvider) -> Unit,
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
        // A payin method exists once the status is pending or active (pending is a bank activation
        // still settling); either way the user has connected a method, so both show the check. This
        // is derived straight from the live status, so moving forward and back never claims a
        // connection the backend does not report.
        val isConnected = content.payinStatus != OnboardingPayinStatus.NeedsSetup
        val canSkip = !isConnected && content.hasAttemptedToConnect
        Spacer(Modifier.height(16.dp))
        // Keep the subtitle in every state so the header height (and the graphic below it) never
        // shifts vertically as the status changes.
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_TITLE),
          description = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        OnboardingConnectingPaymentSymbol(
          provider = content.selectedProvider,
          showCheck = isConnected,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.weight(1f))
        if (!isConnected) {
          HedvigText(
            // TODO: Add "Required to keep your insurance active" /
            //  "Krävs för att din försäkring ska vara aktiv" to Lokalise
            text = "Required to keep your insurance active",
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
          RadioGroup(
            options = content.availableProviders.map { it.toRadioOption() },
            selectedOption = content.selectedProvider?.let { RadioOptionId(it.name) },
            onRadioOptionSelected = { id ->
              val provider = content.availableProviders.firstOrNull { it.name == id.id }
              if (provider != null) onProviderSelected(provider)
            },
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        OnboardingStepButtons(
          primaryText = if (isConnected) {
            stringResource(Res.string.general_continue_button)
          } else {
            stringResource(Res.string.PAYMENT_CONNECT_TITLE)
          },
          onPrimaryClick = if (isConnected) onContinue else onConnectPayment,
          primaryEnabled = isConnected || content.selectedProvider != null,
          secondaryText = if (canSkip) stringResource(Res.string.ONBOARDING_DO_THIS_LATER_BUTTON) else null,
          onSecondaryClick = if (canSkip) onContinue else null,
          caption = if (isConnected) {
            stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_SWITCH_ACCOUNTS_LATER)
          } else {
            null
          },
        )
      }
    }
  }
}

@Composable
private fun OnboardingPayinProvider.toRadioOption(): RadioOption = when (this) {
  OnboardingPayinProvider.Trustly -> RadioOption(
    id = RadioOptionId(name),
    text = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_BANK_LABEL),
    label = stringResource(Res.string.PAYMENT_OPTION_TRUSTLY_SUBTITLE),
    iconResource = IconResource.Vector(HedvigIcons.Trustly),
  )

  OnboardingPayinProvider.Swish -> RadioOption(
    id = RadioOptionId(name),
    text = stringResource(Res.string.swish),
    label = stringResource(Res.string.PAYMENT_OPTION_SWISH_SUBTITLE),
    iconResource = IconResource.Vector(HedvigIcons.Swish),
  )
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
        onProviderSelected = {},
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
      availableProviders = OnboardingPayinProvider.entries,
    ),
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.NeedsSetup,
      availableProviders = OnboardingPayinProvider.entries,
      selectedProvider = OnboardingPayinProvider.Swish,
      hasAttemptedToConnect = true,
    ),
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.Pending,
      availableProviders = OnboardingPayinProvider.entries,
    ),
    OnboardingPaymentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 3),
      payinStatus = OnboardingPayinStatus.Active,
      availableProviders = OnboardingPayinProvider.entries,
    ),
  ),
)
