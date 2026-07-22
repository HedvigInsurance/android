package com.hedvig.android.feature.onboarding.ui.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Surface
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
internal class OnboardingInviteViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
) : MoleculeViewModel<OnboardingInviteEvent, OnboardingInviteUiState>(
    initialState = OnboardingInviteUiState.Loading,
    presenter = OnboardingInvitePresenter(sessionStore, navigator),
  )

internal class OnboardingInvitePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingInviteEvent, OnboardingInviteUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingInviteEvent>.present(
    lastState: OnboardingInviteUiState,
  ): OnboardingInviteUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingInviteUiState.Content) return@LaunchedEffect
      currentState = OnboardingInviteUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingInviteUiState.Error },
        ifRight = { session ->
          val referral = session.data.referralInformation
          currentState = if (referral == null) {
            OnboardingInviteUiState.Error
          } else {
            OnboardingInviteUiState.Content(
              progress = session.progressFor(OnboardingStepId.InviteFriend),
              incentiveDisplay = "${referral.monthlyDiscountPerReferralAmount.toInt()} ${referral.currencyCode}",
            )
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingInviteEvent.Retry -> loadIteration++
        OnboardingInviteEvent.Close -> launch { navigator.exitOnboarding() }
        OnboardingInviteEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.InviteFriend) }
        OnboardingInviteEvent.InviteFriend -> navigator.openForeverScreen()
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingInviteUiState {
  data object Loading : OnboardingInviteUiState

  data object Error : OnboardingInviteUiState

  data class Content(
    val progress: OnboardingProgress,
    val incentiveDisplay: String,
  ) : OnboardingInviteUiState
}

internal sealed interface OnboardingInviteEvent {
  data object Retry : OnboardingInviteEvent

  data object Close : OnboardingInviteEvent

  data object Continue : OnboardingInviteEvent

  data object InviteFriend : OnboardingInviteEvent
}

@Composable
internal fun OnboardingInviteDestination(viewModel: OnboardingInviteViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingInviteUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingInviteEvent.Close) },
  ) {
    when (val state = uiState) {
      OnboardingInviteUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingInviteUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingInviteEvent.Retry) },
        )
      }

      is OnboardingInviteUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          // TODO: Add "Invite a friend" / "Bjud in en vän" to Lokalise
          title = "Invite a friend",
          // TODO: Add the body copy below (and its Swedish translation) to Lokalise
          description = "With Hedvig Forever, you get ${state.incentiveDisplay} off for every friend you invite",
        )
        Spacer(Modifier.weight(1f))
        ExampleReferralsCard(
          incentiveDisplay = state.incentiveDisplay,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.weight(1f))
        OnboardingStepButtons(
          // TODO: Add "Continue" / "Fortsätt" to Lokalise
          primaryText = "Continue",
          onPrimaryClick = { viewModel.emit(OnboardingInviteEvent.Continue) },
          // TODO: Add "Invite a friend" / "Bjud in en vän" to Lokalise
          secondaryText = "Invite a friend",
          secondaryAbovePrimary = true,
          onSecondaryClick = { viewModel.emit(OnboardingInviteEvent.InviteFriend) },
        )
      }
    }
  }
}

@Composable
private fun ExampleReferralsCard(incentiveDisplay: String, modifier: Modifier = Modifier) {
  // Illustrative, hardcoded example names showing what the referral list looks like once populated.
  val exampleNames = listOf("Hampus", "Li", "Elin")
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
  ) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
      exampleNames.forEachIndexed { index, name ->
        if (index > 0) {
          HorizontalDivider()
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          Box(
            Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(HedvigTheme.colorScheme.signalGreenElement),
          )
          Spacer(Modifier.width(8.dp))
          HedvigText(text = name, style = HedvigTheme.typography.bodySmall)
          Spacer(Modifier.width(32.dp).weight(1f))
          HedvigText(text = "-$incentiveDisplay", style = HedvigTheme.typography.bodySmall)
        }
      }
    }
  }
}
