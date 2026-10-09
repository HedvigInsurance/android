package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.feature.onboarding.ui.withOnboardingHaptic
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.ui.analytics.consent.AnalyticsConsentContent
import com.hedvig.android.ui.analytics.consent.ConsentBadge
import com.hedvig.android.ui.analytics.consent.rememberAnalyticsConsentDecision
import dev.zacsweers.metro.Inject
import hedvig.resources.LEGAL_PRIVACY_POLICY_APP_SHORT
import hedvig.resources.ONBOARDING_ANALYTICS_ALLOW_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_DENY_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_SUBTITLE
import hedvig.resources.ONBOARDING_ANALYTICS_TITLE
import hedvig.resources.Res
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingConsentViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  settingsDataStore: SettingsDataStore,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingConsentEvent, OnboardingConsentUiState>(
    initialState = OnboardingConsentUiState.Loading,
    presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore),
  )

internal class OnboardingConsentPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val settingsDataStore: SettingsDataStore,
) : MoleculePresenter<OnboardingConsentEvent, OnboardingConsentUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingConsentEvent>.present(
    lastState: OnboardingConsentUiState,
  ): OnboardingConsentUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var exiting by remember { mutableStateOf(false) }
    val decision = rememberAnalyticsConsentDecision(
      settingsDataStore = settingsDataStore,
      lastConsent = (lastState as? OnboardingConsentUiState.Content)?.consent,
      onDecided = { navigator.continueFrom(OnboardingStepId.AnalyticsConsent) },
    )

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingConsentUiState.Content) return@LaunchedEffect
      currentState = OnboardingConsentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingConsentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingConsentUiState.Content(
            progress = session.progressFor(OnboardingStepId.AnalyticsConsent),
            consent = AnalyticsConsent.NOT_DECIDED,
            buttonsEnabled = true,
          )
        },
      )
    }

    LaunchedEffect(exiting) {
      if (!exiting) return@LaunchedEffect
      navigator.exitOnboarding()
      exiting = false
    }

    CollectEvents { event ->
      when (event) {
        OnboardingConsentEvent.Retry -> {
          loadIteration++
        }

        OnboardingConsentEvent.Close -> {
          if (!decision.isDeciding) exiting = true
        }

        OnboardingConsentEvent.Allow -> {
          if (!exiting) decision.decide(AnalyticsConsent.GRANTED)
        }

        OnboardingConsentEvent.Deny -> {
          if (!exiting) decision.decide(AnalyticsConsent.DENIED)
        }

        is OnboardingConsentEvent.BadgeSettled -> {
          decision.onBadgeSettled(event.badge)
        }
      }
    }

    return when (val state = currentState) {
      is OnboardingConsentUiState.Content -> {
        // Held back until the stored answer is known, so the card does not animate in a badge that was
        // already there.
        val consent = decision.consent ?: return OnboardingConsentUiState.Loading
        state.copy(
          consent = consent,
          buttonsEnabled = !decision.isDeciding && !exiting,
        )
      }

      else -> {
        state
      }
    }
  }
}

internal sealed interface OnboardingConsentUiState {
  data object Loading : OnboardingConsentUiState

  data object Error : OnboardingConsentUiState

  data class Content(
    val progress: OnboardingProgress,
    val consent: AnalyticsConsent,
    val buttonsEnabled: Boolean,
  ) : OnboardingConsentUiState {
    val badge: ConsentBadge?
      get() = ConsentBadge.from(consent)
  }
}

internal sealed interface OnboardingConsentEvent {
  data object Retry : OnboardingConsentEvent

  data object Close : OnboardingConsentEvent

  data object Allow : OnboardingConsentEvent

  data object Deny : OnboardingConsentEvent

  /** The card's badge has finished animating to [badge]. */
  data class BadgeSettled(val badge: ConsentBadge?) : OnboardingConsentEvent
}

@Composable
internal fun OnboardingConsentDestination(
  viewModel: OnboardingConsentViewModel,
  navigateUp: () -> Unit,
  openPrivacyPolicy: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingConsentScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    openPrivacyPolicy = openPrivacyPolicy,
    onClose = { viewModel.emit(OnboardingConsentEvent.Close) },
    onRetry = { viewModel.emit(OnboardingConsentEvent.Retry) },
    onAllow = { viewModel.emit(OnboardingConsentEvent.Allow) },
    onDeny = { viewModel.emit(OnboardingConsentEvent.Deny) },
    onBadgeSettled = { badge ->
      viewModel.emit(OnboardingConsentEvent.BadgeSettled(badge))
    },
  )
}

@Composable
private fun OnboardingConsentScreen(
  uiState: OnboardingConsentUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  openPrivacyPolicy: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onAllow: () -> Unit,
  onDeny: () -> Unit,
  onBadgeSettled: (badge: ConsentBadge?) -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingConsentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (uiState) {
      OnboardingConsentUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingConsentUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingConsentUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        AnalyticsConsentContent(
          badge = uiState.badge,
          buttonsEnabled = uiState.buttonsEnabled,
          onBadgeSettled = onBadgeSettled,
          onAllow = withOnboardingHaptic(onAllow),
          onDeny = withOnboardingHaptic(onDeny),
          onPrivacyPolicy = openPrivacyPolicy,
        )
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingConsentScreen(
  @PreviewParameter(OnboardingConsentUiStateProvider::class) uiState: OnboardingConsentUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingConsentScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        openPrivacyPolicy = {},
        onClose = {},
        onRetry = {},
        onAllow = {},
        onDeny = {},
        onBadgeSettled = {},
      )
    }
  }
}

private class OnboardingConsentUiStateProvider : CollectionPreviewParameterProvider<OnboardingConsentUiState>(
  listOf(
    OnboardingConsentUiState.Loading,
    OnboardingConsentUiState.Error,
    OnboardingConsentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      consent = AnalyticsConsent.NOT_DECIDED,
      buttonsEnabled = true,
    ),
    OnboardingConsentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      consent = AnalyticsConsent.GRANTED,
      buttonsEnabled = false,
    ),
    OnboardingConsentUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      consent = AnalyticsConsent.DENIED,
      buttonsEnabled = false,
    ),
  ),
)
