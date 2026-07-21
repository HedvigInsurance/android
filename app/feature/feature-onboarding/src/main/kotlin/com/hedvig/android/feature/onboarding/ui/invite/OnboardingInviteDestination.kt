package com.hedvig.android.feature.onboarding.ui.invite

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
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
              code = referral.code,
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
    val code: String,
    val incentiveDisplay: String,
  ) : OnboardingInviteUiState
}

internal sealed interface OnboardingInviteEvent {
  data object Retry : OnboardingInviteEvent

  data object Close : OnboardingInviteEvent

  data object Continue : OnboardingInviteEvent
}

@Composable
internal fun OnboardingInviteDestination(viewModel: OnboardingInviteViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
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
        val content = state
        Spacer(Modifier.height(8.dp))
        OnboardingStepHeader(
          // TODO: Add "Invite a friend" / "Bjud in en vän" to Lokalise
          title = "Invite a friend",
          // TODO: Add the body copy below (and its Swedish translation) to Lokalise
          description = "With Hedvig Forever, you get ${content.incentiveDisplay} off for every friend you invite",
        )
        Spacer(Modifier.weight(1f))
        HedvigText(
          text = content.code,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.weight(1f))
        OnboardingStepButtons(
          // TODO: Add "Continue" / "Fortsätt" to Lokalise
          primaryText = "Continue",
          onPrimaryClick = { viewModel.emit(OnboardingInviteEvent.Continue) },
          // TODO: Add "Invite a friend" / "Bjud in en vän" to Lokalise
          secondaryText = "Invite a friend",
          onSecondaryClick = {
            // TODO: Add the share message to Lokalise; align copy with the Forever screen's
            //  REFERRAL_SMS_MESSAGE once product confirms.
            context.showShareSheet(
              title = "Invite a friend",
              text = "With my code you get a discount at Hedvig. Use code ${content.code}.",
            )
          },
        )
      }
    }
  }
}

private fun Context.showShareSheet(title: String, text: String) {
  startActivity(
    Intent.createChooser(
      Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
      },
      title,
    ),
  )
}
