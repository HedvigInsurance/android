package com.hedvig.android.feature.addon.purchase.ui.selectinsurance

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.DoubleTitleHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonState.Failure
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonState.Loading
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonState.Success
import hedvig.resources.R

@Composable
internal fun SelectInsuranceForAddonDestination(
  viewModel: SelectInsuranceForAddonViewModel,
  navigateUp: () -> Unit,
  navigateToCustomizeAddon: (chosenInsuranceId: String) -> Unit,
) {
  val uiState: SelectInsuranceForAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectInsuranceForAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToCustomizeAddon = { id ->
      navigateToCustomizeAddon(id)
      viewModel.emit(SelectInsuranceForAddonEvent.ClearNavigation)
    },
    selectInsurance = { selected ->
      viewModel.emit(SelectInsuranceForAddonEvent.SelectInsurance(selected))
    },
    submitSelected = { selected ->
      viewModel.emit(SelectInsuranceForAddonEvent.SubmitSelected(selected))
    },
    reload = {
      viewModel.emit(SelectInsuranceForAddonEvent.Reload)
    },
  )
}

@Composable
private fun SelectInsuranceForAddonScreen(
  uiState: SelectInsuranceForAddonState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  selectInsurance: (selected: InsuranceForAddon) -> Unit,
  submitSelected: (selected: InsuranceForAddon) -> Unit,
  navigateToCustomizeAddon: (chosenInsuranceId: String) -> Unit,
) {
  when (uiState) {
    SelectInsuranceForAddonState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    SelectInsuranceForAddonState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is SelectInsuranceForAddonState.Success -> {
      LaunchedEffect(uiState.insuranceIdToContinue) {
        if (uiState.insuranceIdToContinue != null) {
          navigateToCustomizeAddon(uiState.insuranceIdToContinue)
        }
      }
      SelectInsuranceForAddonContentScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        selectInsurance = selectInsurance,
        submitSelected = submitSelected,
      )
    }
  }
}

@Composable
private fun SelectInsuranceForAddonContentScreen(
  uiState: SelectInsuranceForAddonState.Success,
  navigateUp: () -> Unit,
  selectInsurance: (selected: InsuranceForAddon) -> Unit,
  submitSelected: (selected: InsuranceForAddon) -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { navigateUp() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    DoubleTitleHeading(
      stringResource(R.string.ADDON_FLOW_TITLE),
      stringResource(R.string.ADDON_FLOW_SELECT_INSURANCE_SUBTITLE),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    val radioOptionData = uiState.listOfInsurances.toListOfDataWithLabel(uiState.currentlySelected?.id)
    RadioGroup(
      onOptionClick = { insuranceId -> selectInsurance(uiState.listOfInsurances.first { it.id == insuranceId }) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      radioGroupSize = RadioGroupSize.Medium,
      radioGroupStyle = RadioGroupStyle.Vertical.Label(radioOptionData),
    )
    Spacer(Modifier.height(12.dp))
    HedvigButton(
      stringResource(id = R.string.general_continue_button),
      enabled = uiState.currentlySelected != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = { uiState.currentlySelected?.let { submitSelected(it) } },
      isLoading = false,
    )
    Spacer(Modifier.height(16.dp))
  }
}

private fun List<InsuranceForAddon>.toListOfDataWithLabel(
  selectedInsuranceId: String?,
): List<RadioOptionGroupDataWithLabel> {
  return this.map { i ->
    RadioOptionGroupDataWithLabel(
      RadioOptionData(
        id = i.id,
        optionText = i.displayName,
        chosenState = if (selectedInsuranceId == i.id) Chosen else NotChosen,
      ),
      labelText = i.contractExposure,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    ChooseInsuranceForAddonUiStateProvider::class,
  ) uiState: SelectInsuranceForAddonState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectInsuranceForAddonScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceForAddonUiStateProvider :
  CollectionPreviewParameterProvider<SelectInsuranceForAddonState>(
    listOf(
      Success(
        listOfInsurances = listOf(
          InsuranceForAddon(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          InsuranceForAddon(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        currentlySelected = null,
        insuranceIdToContinue = null,
      ),
      Success(
        listOfInsurances = listOf(
          InsuranceForAddon(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          InsuranceForAddon(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        currentlySelected = InsuranceForAddon(
          id = "1",
          displayName = "HomeownerInsurance",
          contractExposure = "Opulullegatan 19",
          contractGroup = ContractGroup.HOUSE,
        ),
        insuranceIdToContinue = null,
      ),
      Failure,
      Loading,
    ),
  )
