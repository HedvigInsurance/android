package com.hedvig.feature.remove.addons.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.feature.remove.addons.data.InsuranceForAddon
import hedvig.resources.REMOVE_ADDONS_FLOW_NO_ELIGIBLE_INSURANCES
import hedvig.resources.REMOVE_ADDON_OFFER_PAGE_TITLE
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_SELECT_INSURANCE_SUBTITLE
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SelectInsuranceToRemoveAddonDestination(
  navigateUp: () -> Unit,
  navigateToChooseAddon: (ContractId) -> Unit,
) {
  val viewModel: SelectInsuranceToRemoveAddonViewModel = koinViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectInsuranceToRemoveAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToChooseAddon = { id ->
      navigateToChooseAddon(id)
      viewModel.emit(SelectInsuranceToRemoveAddonEvent.ClearNavigation)
    },
    selectInsurance = { selected ->
      viewModel.emit(SelectInsuranceToRemoveAddonEvent.SelectInsurance(selected))
    },
    submitSelected = { selected ->
      viewModel.emit(SelectInsuranceToRemoveAddonEvent.SubmitSelected(selected))
    },
    reload = {
      viewModel.emit(SelectInsuranceToRemoveAddonEvent.Reload)
    },
  )
}

@Composable
private fun SelectInsuranceToRemoveAddonScreen(
  uiState: SelectInsuranceToRemoveAddonState,
  navigateUp: () -> Unit,
  navigateToChooseAddon: (ContractId) -> Unit,
  selectInsurance: (ContractId) -> Unit,
  submitSelected: (ContractId) -> Unit,
  reload: () -> Unit,
) {
  when (uiState) {
    SelectInsuranceToRemoveAddonState.Error -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    SelectInsuranceToRemoveAddonState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is SelectInsuranceToRemoveAddonState.Success -> {
      LaunchedEffect(uiState.insuranceIdToContinue) {
        if (uiState.insuranceIdToContinue != null) {
          navigateToChooseAddon(uiState.insuranceIdToContinue)
        }
      }
      SelectInsuranceToRemoveAddonContentScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        selectInsurance = selectInsurance,
        submitSelected = submitSelected,
      )
    }

    SelectInsuranceToRemoveAddonState.EmptyList -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          title = stringResource(Res.string.REMOVE_ADDONS_FLOW_NO_ELIGIBLE_INSURANCES),
          subTitle = null,
          onButtonClick = reload,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun SelectInsuranceToRemoveAddonContentScreen(
  uiState: SelectInsuranceToRemoveAddonState.Success,
  selectInsurance: (ContractId) -> Unit,
  submitSelected: (ContractId) -> Unit,
  navigateUp: () -> Unit,
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
            contentDescription = stringResource(Res.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowHeading(
      stringResource(Res.string.REMOVE_ADDON_OFFER_PAGE_TITLE),
      stringResource(Res.string.TIER_FLOW_SELECT_INSURANCE_SUBTITLE),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    RadioGroup(
      options = uiState.listOfInsurances.map { insuranceForAddon ->
        RadioOption(
          id = RadioOptionId(insuranceForAddon.contractId.id),
          text = insuranceForAddon.displayName,
          label = insuranceForAddon.contractExposure,
        )
      },
      selectedOption = uiState.currentlySelected?.contractId?.let { RadioOptionId(it.id) },
      onRadioOptionSelected = { radioOptionId ->
        selectInsurance(
          uiState.listOfInsurances
            .map(InsuranceForAddon::contractId)
            .first { it.id == radioOptionId.id },
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(12.dp))
    HedvigButton(
      stringResource(Res.string.general_continue_button),
      enabled = uiState.currentlySelected != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = { uiState.currentlySelected?.let { submitSelected(it.contractId) } },
      isLoading = false,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToRemoveAddonScreen(
  @PreviewParameter(
    ChooseInsuranceToRemoveAddonUiStateProvider::class,
  ) uiState: SelectInsuranceToRemoveAddonState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectInsuranceToRemoveAddonScreen(
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

private class ChooseInsuranceToRemoveAddonUiStateProvider :
  CollectionPreviewParameterProvider<SelectInsuranceToRemoveAddonState>(
    listOf(
      SelectInsuranceToRemoveAddonState.Success(
        listOfInsurances = listOf(
          InsuranceForAddon(
            contractId = ContractId("1"),
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          InsuranceForAddon(
            contractId = ContractId("3"),
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        currentlySelected = null,
        insuranceIdToContinue = null,
      ),
      SelectInsuranceToRemoveAddonState.Success(
        listOfInsurances = listOf(
          InsuranceForAddon(
            contractId = ContractId("1"),
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          InsuranceForAddon(
            contractId = ContractId("3"),
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        currentlySelected = InsuranceForAddon(
          contractId = ContractId("1"),
          displayName = "HomeownerInsurance",
          contractExposure = "Opulullegatan 19",
          contractGroup = ContractGroup.HOUSE,
        ),
        insuranceIdToContinue = null,
      ),
      SelectInsuranceToRemoveAddonState.Error,
      SelectInsuranceToRemoveAddonState.Loading,
    ),
  )
