package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
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
    Column(Modifier.weight(1f)) {
      HedvigText(
        text = stringResource(R.string.TIER_FLOW_TITLE),
        style = HedvigTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      HedvigText(
        text = stringResource(R.string.TIER_FLOW_SELECT_COVERAGE_TITLE),
        style = HedvigTheme.typography.bodyMedium,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(8.dp))
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
      ) {
        Spacer(Modifier.height(8.dp))
        CoverageCard(content.tiersInfo, onSelectCoverageOption, onSelectDeductibleOption)
        Spacer(Modifier.height(8.dp))
        // todo Add comparison API results here
        if (content.tiersInfo.coverageOptions.isNotEmpty()) {
          HedvigTextButton(
            text = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
            modifier = Modifier.fillMaxWidth(),
          ) {
            // onCompareCoverageClicked()
          }
          Spacer(Modifier.height(4.dp))
        }
        HedvigButton(
          text = stringResource(R.string.general_continue_button),
          onClick = dropUnlessResumed {
            onSubmit?.invoke()
          },
          enabled = onSubmit != null && content.canSubmit,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun CoverageCard(
  tiersInfo: TiersInfo,
  onSelectCoverageOption: (String) -> Unit,
  onSelectDeductibleOption: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(16.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(tiersInfo.selectedCoverage.productVariant.contractGroup.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          HedvigText(
            text = tiersInfo.selectedCoverage.productVariant.displayName,
          )
          HedvigText(
            text = tiersInfo.selectedCoverage.exposureName,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DropdownWithDialog(
          style = Label(
            items = tiersInfo.coverageOptions.map { coverageInfo ->
              SimpleDropdownItem(coverageInfo.tierName)
            },
            label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
          ),
          size = Small,
          hintText = tiersInfo.selectedCoverage.tierName,
          onItemChosen = {
            onSelectCoverageOption(tiersInfo.coverageOptions[it].moveHomeQuoteId)
          },
          chosenItemIndex = tiersInfo.coverageOptions.indexOfFirst {
            it.moveHomeQuoteId == tiersInfo.selectedCoverage.id
          }.let {
            if (it == -1) null else it
          },
          onSelectorClick = {},
        )
        when (val deductibleOptions = tiersInfo.deductibleOptions) {
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
              hintText = tiersInfo.selectedDeductible?.tierName
                ?: stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PLACEHOLDER),
              onItemChosen = {
                onSelectDeductibleOption(deductibleOptions.deductibleOptions[it].homeQuoteId)
              },
              chosenItemIndex = deductibleOptions.deductibleOptions.indexOfFirst {
                it.homeQuoteId == tiersInfo.selectedDeductible?.id
              }.let {
                if (it == -1) null else it
              },
              onSelectorClick = {},
            )
          }

          is OneOption -> {
            HedvigTextField(
              text = deductibleOptions.deductibleOption.deductible.displayText,
              onValueChange = {},
              textFieldSize = TextFieldSize.Small,
              labelText = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
              readOnly = true,
            )
          }
        }
      }
      HorizontalItemsWithMaximumSpaceTaken(
        { HedvigText(stringResource(R.string.CHANGE_ADDRESS_TOTAL)) },
        {
          HedvigText(
            text = stringResource(
              R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              tiersInfo.selectedCoverage.premium.toString(),
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.wrapContentWidth(Alignment.End),
          )
        },
        Modifier.fillMaxWidth(),
      )
    }
  }
}
