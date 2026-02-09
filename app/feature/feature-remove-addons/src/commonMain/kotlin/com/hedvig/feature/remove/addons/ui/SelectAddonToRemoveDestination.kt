package com.hedvig.feature.remove.addons.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SelectAddonToRemoveDestination(
  navigateUp: () -> Unit,
  navigateToSummary: (
    contractId: String, addonIds: List<String>, activationDate: LocalDate, baseCost: ItemCost,
    currentTotalCost: ItemCost,
  ) -> Unit,
) {
  val viewModel: SelectAddonToRemoveViewModel = koinViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectAddonToRemoveScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = {
      viewModel.emit(SelectAddonToRemoveEvent.Retry)
    },
    navigateToSummary = { params ->
      navigateToSummary(
        params.contractId,
        params.addonIds,
        params.activationDate,
        params.baseCost,
        params.currentTotalCost,
      )
    },
  )
}

@Composable
private fun SelectAddonToRemoveScreen(
  uiState: SelectAddonToRemoveState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  navigateToSummary: (params: CommonSummaryParameters) -> Unit,
) {
  when (uiState) {
    is SelectAddonToRemoveState.Error -> HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      HedvigErrorSection(
        title = uiState.message ?: stringResource(Res.string.something_went_wrong),
        onButtonClick = reload,
        modifier = Modifier.weight(1f),
      )
    }

    is SelectAddonToRemoveState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is SelectAddonToRemoveState.Success -> SelectAddonToRemoveSuccessScreen(
      uiState = uiState,
      navigateUp = navigateUp,
      navigateToSummary = navigateToSummary,
    )
  }
}

@Composable
private fun SelectAddonToRemoveSuccessScreen(
  uiState: SelectAddonToRemoveState.Success,
  navigateUp: () -> Unit,
  navigateToSummary: (params: CommonSummaryParameters) -> Unit,
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


