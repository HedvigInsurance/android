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
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
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
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import hedvig.resources.LEGAL_PRIVACY_POLICY
import hedvig.resources.ONBOARDING_ANALYTICS_ALLOW_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_DENY_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_SUBTITLE
import hedvig.resources.ONBOARDING_ANALYTICS_TITLE
import hedvig.resources.Res
import kotlinx.coroutines.launch
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

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingConsentUiState.Content) return@LaunchedEffect
      currentState = OnboardingConsentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingConsentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingConsentUiState.Content(session.progressFor(OnboardingStepId.AnalyticsConsent))
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingConsentEvent.Retry -> loadIteration++

        OnboardingConsentEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingConsentEvent.Allow -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.GRANTED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }

        OnboardingConsentEvent.Deny -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.DENIED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingConsentUiState {
  data object Loading : OnboardingConsentUiState

  data object Error : OnboardingConsentUiState

  data class Content(val progress: OnboardingProgress) : OnboardingConsentUiState
}

internal sealed interface OnboardingConsentEvent {
  data object Retry : OnboardingConsentEvent

  data object Close : OnboardingConsentEvent

  data object Allow : OnboardingConsentEvent

  data object Deny : OnboardingConsentEvent
}

@Composable
internal fun OnboardingConsentDestination(
  viewModel: OnboardingConsentViewModel,
  navigateUp: () -> Unit,
  openPrivacyPolicy: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingConsentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingConsentEvent.Close) },
    progressAnimation = viewModel.progressBarAnimation,
  ) {
    when (uiState) {
      OnboardingConsentUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingConsentUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingConsentEvent.Retry) },
        )
      }

      is OnboardingConsentUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_ANALYTICS_TITLE),
          description = stringResource(Res.string.ONBOARDING_ANALYTICS_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingVerifiedCard(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .clickable(onClick = openPrivacyPolicy)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          HedvigText(
            text = stringResource(Res.string.LEGAL_PRIVACY_POLICY),
            style = HedvigTheme.typography.bodySmall,
            textDecoration = TextDecoration.Underline,
          )
          Icon(
            imageVector = HedvigIcons.ArrowNorthEast,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.ONBOARDING_ANALYTICS_ALLOW_BUTTON),
          onClick = { viewModel.emit(OnboardingConsentEvent.Allow) },
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(CircleShape),
        )
        Spacer(Modifier.height(8.dp))
        HedvigButton(
          text = stringResource(Res.string.ONBOARDING_ANALYTICS_DENY_BUTTON),
          onClick = { viewModel.emit(OnboardingConsentEvent.Deny) },
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(CircleShape),
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}
