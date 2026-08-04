package com.hedvig.android.feature.onboarding.ui.petid

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
import hedvig.resources.ONBOARDING_ADD_INFO_LATER_LABEL
import hedvig.resources.ONBOARDING_ADD_PET_CHIP_IDS_TITLE
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_MISSING_INFO_PET_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPetIdViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingPetIdEvent, OnboardingPetIdUiState>(
    initialState = OnboardingPetIdUiState.Loading,
    presenter = OnboardingPetIdPresenter(sessionStore, navigator),
  )

internal class OnboardingPetIdPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingPetIdEvent, OnboardingPetIdUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingPetIdEvent>.present(
    lastState: OnboardingPetIdUiState,
  ): OnboardingPetIdUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    // Pin which contracts belong to this step the first time data is available, so completed
    // rows render as done instead of disappearing.
    var pinnedContractIds by remember { mutableStateOf<List<String>?>(null) }

    fun contentFrom(session: OnboardingSession): OnboardingPetIdUiState.Content {
      val ids = pinnedContractIds
        ?: session.data.contractsWithMissingPetId.map { it.id }.also { pinnedContractIds = it }
      return OnboardingPetIdUiState.Content(
        progress = session.progressFor(OnboardingStepId.PetIds),
        rows = ids.mapNotNull { id ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          PetIdRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            typeOfContract = contract.typeOfContract,
            isComplete = !contract.isMissingPetId,
          )
        },
      )
    }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPetIdUiState.Content) return@LaunchedEffect
      currentState = OnboardingPetIdUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPetIdUiState.Error },
        ifRight = { session -> currentState = contentFrom(session) },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPetIdEvent.Retry -> loadIteration++

        OnboardingPetIdEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingPetIdEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed -> currentState = contentFrom(refreshed) }
          // onLeft: keep the previously shown state, per the spec's error handling.
        }

        OnboardingPetIdEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.PetIds) }
      }
    }

    return currentState
  }
}

internal data class PetIdRow(
  val contractId: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val isComplete: Boolean,
)

internal sealed interface OnboardingPetIdUiState {
  data object Loading : OnboardingPetIdUiState

  data object Error : OnboardingPetIdUiState

  data class Content(
    val progress: OnboardingProgress,
    val rows: List<PetIdRow>,
  ) : OnboardingPetIdUiState
}

internal sealed interface OnboardingPetIdEvent {
  data object Retry : OnboardingPetIdEvent

  data object Close : OnboardingPetIdEvent

  data object Refresh : OnboardingPetIdEvent

  data object Continue : OnboardingPetIdEvent
}

@Composable
internal fun OnboardingPetIdDestination(
  viewModel: OnboardingPetIdViewModel,
  navigateUp: () -> Unit,
  onAddPetId: (contractId: String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(OnboardingPetIdEvent.Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }

  OnboardingPetIdScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingPetIdEvent.Close) },
    onRetry = { viewModel.emit(OnboardingPetIdEvent.Retry) },
    onContinue = { viewModel.emit(OnboardingPetIdEvent.Continue) },
    onAddPetId = onAddPetId,
  )
}

@Composable
private fun OnboardingPetIdScreen(
  uiState: OnboardingPetIdUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onContinue: () -> Unit,
  onAddPetId: (contractId: String) -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingPetIdUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (val content = uiState) {
      OnboardingPetIdUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingPetIdUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingPetIdUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_ADD_PET_CHIP_IDS_TITLE),
          description = stringResource(Res.string.ONBOARDING_MISSING_INFO_PET_SUBTITLE),
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
              onAddClick = { onAddPetId(row.contractId) },
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
          onPrimaryClick = onContinue,
          secondaryText = stringResource(Res.string.ONBOARDING_DO_THIS_LATER_BUTTON),
          onSecondaryClick = onContinue,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingPetIdScreen(
  @PreviewParameter(OnboardingPetIdUiStateProvider::class) uiState: OnboardingPetIdUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingPetIdScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onContinue = {},
        onAddPetId = {},
      )
    }
  }
}

private class OnboardingPetIdUiStateProvider : CollectionPreviewParameterProvider<OnboardingPetIdUiState>(
  listOf(
    OnboardingPetIdUiState.Loading,
    OnboardingPetIdUiState.Error,
    OnboardingPetIdUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      rows = listOf(
        PetIdRow(
          contractId = "contract-1",
          displayName = "Hedvig Pet",
          exposureName = "Fido",
          typeOfContract = "SE_CAT_BASIC",
          isComplete = false,
        ),
        PetIdRow(
          contractId = "contract-2",
          displayName = "Hedvig Pet",
          exposureName = "Whiskers",
          typeOfContract = "SE_CAT_BASIC",
          isComplete = true,
        ),
      ),
    ),
  ),
)
