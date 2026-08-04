package com.hedvig.android.feature.onboarding.ui.invite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
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
import hedvig.resources.ONBOARDING_INVITE_FRIEND_SUBTITLE
import hedvig.resources.ONBOARDING_INVITE_FRIEND_TITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingInviteViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
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
    var inviteCardAnimationPlayed by remember { mutableStateOf(false) }

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
              // toInt is safe: referral incentives are always whole amounts, per product.
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
        OnboardingInviteEvent.InviteCardAnimationCompleted -> inviteCardAnimationPlayed = true
      }
    }

    return when (val state = currentState) {
      is OnboardingInviteUiState.Content -> state.copy(inviteCardAnimationPlayed = inviteCardAnimationPlayed)
      else -> state
    }
  }
}

internal sealed interface OnboardingInviteUiState {
  data object Loading : OnboardingInviteUiState

  data object Error : OnboardingInviteUiState

  data class Content(
    val progress: OnboardingProgress,
    val incentiveDisplay: String,
    val inviteCardAnimationPlayed: Boolean = false,
  ) : OnboardingInviteUiState
}

internal sealed interface OnboardingInviteEvent {
  data object Retry : OnboardingInviteEvent

  data object Close : OnboardingInviteEvent

  data object Continue : OnboardingInviteEvent

  data object InviteFriend : OnboardingInviteEvent

  data object InviteCardAnimationCompleted : OnboardingInviteEvent
}
