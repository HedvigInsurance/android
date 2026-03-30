package com.hedvig.android.feature.chip.id.ui.selectinsurance

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import hedvig.resources.ADDON_FLOW_SELECT_INSURANCE_SUBTITLE
import hedvig.resources.ADDON_FLOW_SELECT_INSURANCE_TITLE
import hedvig.resources.Res
import hedvig.resources.SELECT_INSURANCE_TO_REMOVE_ADDON_TITLE
import hedvig.resources.TERMINATION_ADDON_COVERAGE_TITLE
import hedvig.resources.TIER_FLOW_SELECT_INSURANCE_SUBTITLE
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectInsuranceForChipIdDestination(
  viewModel: SelectInsuranceForChipIdViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToAddChipId: (contractId: String, popSelectInsurance: Boolean) -> Unit,
) {
  val uiState: SelectInsuranceForChipIdState by viewModel.uiState.collectAsStateWithLifecycle()

  SelectInsuranceForChipIdScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    navigateToAddChipId = { contractId, popSelectInsurance ->
      navigateToAddChipId(contractId, popSelectInsurance)
      viewModel.emit(SelectInsuranceForChipIdEvent.ClearNavigation)
    },
    selectContract = { contract ->
      viewModel.emit(SelectInsuranceForChipIdEvent.SelectContract(contract))
    },
    reload = {
      viewModel.emit(SelectInsuranceForChipIdEvent.Reload)
    },
  )
}

@Composable
private fun SelectInsuranceForChipIdScreen(
  uiState: SelectInsuranceForChipIdState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  reload: () -> Unit,
  selectContract: (PetContractForChipId) -> Unit,
  navigateToAddChipId: (contractId: String, popSelectInsurance: Boolean) -> Unit,
) {
  when (uiState) {
    SelectInsuranceForChipIdState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.padding(16.dp))
      }
    }

    SelectInsuranceForChipIdState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is SelectInsuranceForChipIdState.Success -> {
      LaunchedEffect(uiState.contractIdToContinue) {
        if (uiState.contractIdToContinue != null) {
          navigateToAddChipId(
            uiState.contractIdToContinue,
            uiState.contracts.size == 1,
          )
        }
      }
      if (uiState.contractIdToContinue == null) {
        SelectInsuranceForChipIdContentScreen(
          uiState = uiState,
          navigateUp = navigateUp,
          popBackStack = popBackStack,
          selectInsurance = selectContract,
          navigateToAddChipId = navigateToAddChipId,
        )
      }
    }
  }
}

@Composable
private fun SelectInsuranceForChipIdContentScreen(
  uiState: SelectInsuranceForChipIdState.Success,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  selectInsurance: (selected: PetContractForChipId) -> Unit,
  navigateToAddChipId: (contractId: String, popSelectInsurance: Boolean) -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = dropUnlessResumed { popBackStack() },
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
      stringResource(Res.string.TIER_FLOW_SELECT_INSURANCE_SUBTITLE),
      null,
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    RadioGroup(
      options = uiState.contracts.map { insuranceForAddon ->
        RadioOption(
          id = RadioOptionId(insuranceForAddon.id),
          text = insuranceForAddon.displayName,
          label = insuranceForAddon.contractExposure,
        )
      },
      selectedOption = uiState.selectedContract?.id?.let { RadioOptionId(it) },
      onRadioOptionSelected = { optionId ->
        uiState.contracts.firstOrNull { it.id == optionId.id }?.let {
          selectInsurance(it)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(12.dp))
    HedvigButton(
      stringResource(Res.string.general_continue_button),
      enabled = uiState.selectedContract != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = {
        uiState.selectedContract?.let {
          navigateToAddChipId(it.id, uiState.contracts.size == 1)
        }
      },
      isLoading = false,
    )
    Spacer(Modifier.height(16.dp))
  }
}
