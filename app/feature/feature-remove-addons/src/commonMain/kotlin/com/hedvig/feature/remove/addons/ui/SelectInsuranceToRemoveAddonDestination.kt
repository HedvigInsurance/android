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
import hedvig.resources.ADDON_FLOW_SELECT_INSURANCE_SUBTITLE
import hedvig.resources.ADDON_FLOW_TITLE
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SelectInsuranceToRemoveAddonDestination(

  navigateUp: () -> Unit,
  navigateToChooseAddon: (chosenInsuranceId: String) -> Unit,
){
  val viewModel: SelectInsuranceToRemoveAddonViewModel = koinViewModel() //TODO
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
  navigateToChooseAddon: (chosenInsuranceId: String) -> Unit,
  selectInsurance: (chosenInsuranceId: String) -> Unit,
  submitSelected: (chosenInsuranceId: String) -> Unit,
  reload: () -> Unit
) {
  when (uiState) {
    SelectInsuranceToRemoveAddonState.Error -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    SelectInsuranceToRemoveAddonState.Loading -> HedvigFullScreenCenterAlignedProgress()

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
  }
}

@Composable
private fun SelectInsuranceToRemoveAddonContentScreen(
  uiState: SelectInsuranceToRemoveAddonState.Success,
  selectInsurance: (chosenInsuranceId: String) -> Unit,
  submitSelected: (chosenInsuranceId: String) -> Unit,
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
    //  stringResource(Res.string.ADDON_FLOW_TITLE),
      //stringResource(Res.string.ADDON_FLOW_SELECT_INSURANCE_SUBTITLE),
      "Select insurance TODO", //todo!!!
      "Description TODO", //todo!!
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    RadioGroup(
      options = uiState.listOfInsurances.map { insuranceForAddon ->
        RadioOption(
          id = RadioOptionId(insuranceForAddon.id),
          text = insuranceForAddon.displayName,
          label = insuranceForAddon.contractExposure,
        )
      },
      selectedOption = uiState.currentlySelected?.id?.let { RadioOptionId(it) },
      onRadioOptionSelected = { id ->
        selectInsurance(uiState.listOfInsurances.first { it.id == id.id }.id)
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
      onClick = { uiState.currentlySelected?.let { submitSelected(it.id) } },
      isLoading = false,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
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
      SelectInsuranceToRemoveAddonState.Success(
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
      SelectInsuranceToRemoveAddonState.Error,
      SelectInsuranceToRemoveAddonState.Loading,
    ),
  )
