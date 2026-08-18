package com.hedvig.android.feature.onboarding.ui.petid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.onboarding.ui.OnboardingContractCard
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import hedvig.resources.ONBOARDING_ADD_INFO_LATER_LABEL
import hedvig.resources.ONBOARDING_ADD_PET_CHIP_IDS_TITLE
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_MISSING_INFO_PET_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

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
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
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
