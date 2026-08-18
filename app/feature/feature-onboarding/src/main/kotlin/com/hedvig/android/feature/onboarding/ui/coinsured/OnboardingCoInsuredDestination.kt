package com.hedvig.android.feature.onboarding.ui.coinsured

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
import com.hedvig.android.data.coinsured.CoInsuredFlowType
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
import hedvig.resources.ONBOARDING_ADD_COINSURED_TITLE
import hedvig.resources.ONBOARDING_ADD_COOWNERS_TITLE
import hedvig.resources.ONBOARDING_ADD_INFO_LATER_LABEL
import hedvig.resources.ONBOARDING_DO_THIS_LATER_BUTTON
import hedvig.resources.ONBOARDING_MISSING_INFO_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

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

  OnboardingCoInsuredScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingCoInsuredEvent.Close) },
    onRetry = { viewModel.emit(OnboardingCoInsuredEvent.Retry) },
    onAddCoInsured = {
      contractId,
      flowType,
      ->
      viewModel.emit(OnboardingCoInsuredEvent.AddCoInsured(contractId, flowType))
    },
    onContinue = { viewModel.emit(OnboardingCoInsuredEvent.Continue) },
  )
}

@Composable
private fun OnboardingCoInsuredScreen(
  uiState: OnboardingCoInsuredUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onAddCoInsured: (contractId: String, flowType: CoInsuredFlowType) -> Unit,
  onContinue: () -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingCoInsuredUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (val content = uiState) {
      OnboardingCoInsuredUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingCoInsuredUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
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
              secondaryText = row.secondaryText(),
              typeOfContract = row.typeOfContract,
              isComplete = row.isComplete,
              onAddClick = { onAddCoInsured(row.contractId, row.flowType) },
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

// A complete row lists the people's names; while info is still missing it shows how many there are.
// TODO: Add "%1$d co-insured" / "%1$d medförsäkrade" and "%1$d co-owners" / "%1$d delägare" to Lokalise
private fun CoInsuredRow.secondaryText(): String = if (isComplete && insuredNames.isNotEmpty()) {
  insuredNames.toReadableList()
} else {
  val noun = when {
    flowType == CoInsuredFlowType.CoOwners && insuredCount == 1 -> "co-owner"
    flowType == CoInsuredFlowType.CoOwners -> "co-owners"
    else -> "co-insured"
  }
  "$insuredCount $noun"
}

/** Joins names the way the design shows them: "A", "A & B", "A, B & C". */
private fun List<String>.toReadableList(): String = when (size) {
  0 -> ""
  1 -> single()
  else -> "${dropLast(1).joinToString(", ")} & ${last()}"
}

@HedvigPreview
@Composable
private fun PreviewOnboardingCoInsuredScreen(
  @PreviewParameter(OnboardingCoInsuredUiStateProvider::class) uiState: OnboardingCoInsuredUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingCoInsuredScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onAddCoInsured = { _, _ -> },
        onContinue = {},
      )
    }
  }
}

private class OnboardingCoInsuredUiStateProvider : CollectionPreviewParameterProvider<OnboardingCoInsuredUiState>(
  listOf(
    OnboardingCoInsuredUiState.Loading,
    OnboardingCoInsuredUiState.Error,
    OnboardingCoInsuredUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      rows = listOf(
        CoInsuredRow(
          contractId = "contract-1",
          displayName = "Home Insurance",
          typeOfContract = "SE_APARTMENT_RENT",
          flowType = CoInsuredFlowType.CoInsured,
          isComplete = false,
          insuredCount = 5,
        ),
        CoInsuredRow(
          contractId = "contract-2",
          displayName = "Car Insurance",
          typeOfContract = "SE_CAR_FULL",
          flowType = CoInsuredFlowType.CoInsured,
          isComplete = true,
          insuredCount = 3,
          insuredNames = listOf("Sladan", "Mariia", "Sonny"),
        ),
      ),
    ),
    OnboardingCoInsuredUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      rows = listOf(
        CoInsuredRow(
          contractId = "contract-3",
          displayName = "Villa Insurance",
          typeOfContract = "SE_VILLA",
          flowType = CoInsuredFlowType.CoOwners,
          isComplete = false,
          insuredCount = 2,
        ),
        CoInsuredRow(
          contractId = "contract-4",
          displayName = "Cottage Insurance",
          typeOfContract = "SE_VILLA",
          flowType = CoInsuredFlowType.CoOwners,
          isComplete = false,
          insuredCount = 3,
        ),
      ),
    ),
  ),
)
