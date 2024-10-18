package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Content
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.Loading
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import hedvig.resources.R

@Composable
internal fun ChoseCoverageLevelAndDeductibleDestination(
  viewModel: ChoseCoverageLevelAndDeductibleViewModel,
  navigateUp: () -> Unit,
  onNavigateToSummaryScreen: (homeQuoteId: String) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  ChoseCoverageLevelAndDeductibleScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onSubmit = { selectedHomeQuoteId -> onNavigateToSummaryScreen(selectedHomeQuoteId) },
    onSelectCoverageOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectCoverage(it)) },
    onSelectDeductibleOption = { viewModel.emit(ChoseCoverageLevelAndDeductibleEvent.SelectDeductible(it)) },
  )
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  uiState: ChoseCoverageLevelAndDeductibleUiState,
  navigateUp: () -> Unit,
  onSubmit: (String) -> Unit,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
) {
  when (uiState) {
    Loading -> {
      HedvigText("Loading")
    }

    MissingOngoingMovingFlow -> {
      HedvigText("MissingOngoingMovingFlow")
    }

    is Content -> ChoseCoverageLevelAndDeductibleScreen(
      content = uiState,
      navigateUp = navigateUp,
      onSubmit = uiState.tiersInfo.selectedHomeQuoteId?.let { { onSubmit(it) } },
      onSelectCoverageOption = onSelectCoverageOption,
      onSelectDeductibleOption = onSelectDeductibleOption,
    )
  }
}

@Composable
private fun ChoseCoverageLevelAndDeductibleScreen(
  content: Content,
  navigateUp: () -> Unit,
  onSubmit: (() -> Unit)?,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
) {
  HedvigScaffold(navigateUp) {
    Spacer(Modifier.weight(1f))
    content.tiersInfo.let { newContent ->
      DropdownWithDialog(
        style = Label(
          items = newContent.coverageOptions.map { coverageInfo ->
            SimpleDropdownItem(coverageInfo.tierName)
          },
          label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
        ),
        size = Small,
        hintText = newContent.selectedCoverage.tierName,
        onItemChosen = {
          onSelectCoverageOption(newContent.coverageOptions[it].moveHomeQuoteId)
        },
        chosenItemIndex = newContent.coverageOptions.indexOfFirst {
          it.moveHomeQuoteId == newContent.selectedCoverage.id
        }.let {
          if (it == -1) null else it
        },
        onSelectorClick = {},
      )
    }
    when (val deductibleOptions = content.tiersInfo.deductibleOptions) {
      NoOptions -> {}
      is MutlipleOptions -> {
        DropdownWithDialog(
          style = Label(
            items = deductibleOptions.deductibleOptions.map { coverageInfo ->
              SimpleDropdownItem(coverageInfo.deductible.amount.toString())
            },
            label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
          ),
          size = Small,
          hintText = content.tiersInfo.selectedDeductible?.tierName
            ?: stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
          onItemChosen = {
            onSelectDeductibleOption(deductibleOptions.deductibleOptions[it].homeQuoteId)
          },
          chosenItemIndex = deductibleOptions.deductibleOptions.indexOfFirst {
            it.homeQuoteId == content.tiersInfo.selectedDeductible?.id
          }.let {
            if (it == -1) null else it
          },
          onSelectorClick = {},
        )
      }

      is OneOption -> {
//        DropdownWithDialog(isEnabled = false)
      }
    }
    // if there is coverage to compare
    // HedvigButton()
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = dropUnlessResumed {
        onSubmit?.invoke()
      },
      enabled = onSubmit != null && content.canSubmit,
    )
  }
}
