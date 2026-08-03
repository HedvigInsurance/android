package com.hedvig.android.feature.onboarding.ui.coinsured

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingContractCard
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
import hedvig.resources.ONBOARDING_ADD_COINSURED_TITLE
import hedvig.resources.ONBOARDING_ADD_COOWNERS_TITLE
import hedvig.resources.ONBOARDING_ADD_INFO_LATER_LABEL
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_MISSING_INFO_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingCoInsuredViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingCoInsuredEvent, OnboardingCoInsuredUiState>(
    initialState = OnboardingCoInsuredUiState.Loading,
    presenter = OnboardingCoInsuredPresenter(sessionStore, navigator),
  )

internal class OnboardingCoInsuredPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingCoInsuredEvent, OnboardingCoInsuredUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingCoInsuredEvent>.present(
    lastState: OnboardingCoInsuredUiState,
  ): OnboardingCoInsuredUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    // Pin which contracts belong to this step (and each one's flow type) the first time data is
    // available, so completed rows render as done instead of disappearing and the header title
    // stays stable even after a contract's missing info is filled in.
    var pinnedContracts by remember { mutableStateOf<List<Pair<String, CoInsuredFlowType>>?>(null) }

    fun contentFrom(session: OnboardingSession): OnboardingCoInsuredUiState.Content {
      val pinned = pinnedContracts
        ?: session.data.contractsMissingInsuredOrOwnerInfo
          .mapNotNull { contract -> contract.coInsuredFlowType?.let { contract.id to it } }
          .also { pinnedContracts = it }
      return OnboardingCoInsuredUiState.Content(
        progress = session.progressFor(OnboardingStepId.CoInsured),
        rows = pinned.mapNotNull { (id, flowType) ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          CoInsuredRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            typeOfContract = contract.typeOfContract,
            flowType = flowType,
            isComplete = contract.coInsuredFlowType == null,
          )
        },
      )
    }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingCoInsuredUiState.Content) return@LaunchedEffect
      currentState = OnboardingCoInsuredUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingCoInsuredUiState.Error },
        ifRight = { session -> currentState = contentFrom(session) },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingCoInsuredEvent.Retry -> loadIteration++

        OnboardingCoInsuredEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingCoInsuredEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed -> currentState = contentFrom(refreshed) }
          // onLeft: keep the previously shown state, per the spec's error handling.
        }

        is OnboardingCoInsuredEvent.AddCoInsured -> navigator.openAddCoInsured(event.contractId, event.flowType)

        OnboardingCoInsuredEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.CoInsured) }
      }
    }

    return currentState
  }
}

internal data class CoInsuredRow(
  val contractId: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val flowType: CoInsuredFlowType,
  val isComplete: Boolean,
)

internal sealed interface OnboardingCoInsuredUiState {
  data object Loading : OnboardingCoInsuredUiState

  data object Error : OnboardingCoInsuredUiState

  data class Content(
    val progress: OnboardingProgress,
    val rows: List<CoInsuredRow>,
  ) : OnboardingCoInsuredUiState
}

internal sealed interface OnboardingCoInsuredEvent {
  data object Retry : OnboardingCoInsuredEvent

  data object Close : OnboardingCoInsuredEvent

  data object Refresh : OnboardingCoInsuredEvent

  data class AddCoInsured(val contractId: String, val flowType: CoInsuredFlowType) : OnboardingCoInsuredEvent

  data object Continue : OnboardingCoInsuredEvent
}

@Composable
internal fun OnboardingCoInsuredDestination(viewModel: OnboardingCoInsuredViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(OnboardingCoInsuredEvent.Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }

  OnboardingStepScaffold(
    progress = (uiState as? OnboardingCoInsuredUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingCoInsuredEvent.Close) },
    progressAnimation = viewModel.progressBarAnimation,
  ) {
    when (val content = uiState) {
      OnboardingCoInsuredUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingCoInsuredUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingCoInsuredEvent.Retry) },
        )
      }

      is OnboardingCoInsuredUiState.Content -> {
        // The step has a single title but its rows can mix types, so use the co-owners title only
        // when every row is co-owners and fall back to co-insured otherwise (including the mix).
        val allCoOwners = content.rows.isNotEmpty() && content.rows.all { it.flowType == CoInsuredFlowType.CoOwners }
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = if (allCoOwners) {
            stringResource(Res.string.ONBOARDING_ADD_COOWNERS_TITLE)
          } else {
            stringResource(Res.string.ONBOARDING_ADD_COINSURED_TITLE)
          },
          description = stringResource(Res.string.ONBOARDING_MISSING_INFO_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          for (row in content.rows) {
            OnboardingContractCard(
              displayName = row.displayName,
              exposureName = row.exposureName,
              typeOfContract = row.typeOfContract,
              isComplete = row.isComplete,
              onAddClick = { viewModel.emit(OnboardingCoInsuredEvent.AddCoInsured(row.contractId, row.flowType)) },
            )
          }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        HedvigText(
          text = stringResource(Res.string.ONBOARDING_ADD_INFO_LATER_LABEL),
          style = HedvigTheme.typography.finePrint,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.general_continue_button),
          onPrimaryClick = { viewModel.emit(OnboardingCoInsuredEvent.Continue) },
          secondaryText = stringResource(Res.string.ONBOARDING_DO_THIS_LATER_BUTTON),
          onSecondaryClick = { viewModel.emit(OnboardingCoInsuredEvent.Continue) },
        )
      }
    }
  }
}
