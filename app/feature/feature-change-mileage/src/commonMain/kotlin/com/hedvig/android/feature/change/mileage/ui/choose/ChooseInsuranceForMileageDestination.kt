package com.hedvig.android.feature.change.mileage.ui.choose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.mileage.data.MileageContract
import hedvig.resources.CHANGE_MILEAGE_EMPTY_STATE
import hedvig.resources.CHANGE_MILEAGE_OPTION_TITLE
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_SELECT_INSURANCE_SUBTITLE
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChooseInsuranceForMileageDestination(
  viewModel: ChooseInsuranceForMileageViewModel,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
) {
  val uiState: ChooseInsuranceForMileageUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseInsuranceForMileageScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackstack = popBackstack,
    reload = { viewModel.emit(ChooseInsuranceForMileageEvent.Reload) },
    selectInsurance = { viewModel.emit(ChooseInsuranceForMileageEvent.SelectInsurance(it)) },
    onContinue = { viewModel.emit(ChooseInsuranceForMileageEvent.Continue) },
  )
}

@Composable
private fun ChooseInsuranceForMileageScreen(
  uiState: ChooseInsuranceForMileageUiState,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  reload: () -> Unit,
  selectInsurance: (contractId: String) -> Unit,
  onContinue: () -> Unit,
) {
  when (uiState) {
    ChooseInsuranceForMileageUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    ChooseInsuranceForMileageUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    ChooseInsuranceForMileageUiState.NoEligibleInsurances -> {
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = stringResource(Res.string.CHANGE_MILEAGE_EMPTY_STATE),
          description = null,
          iconStyle = INFO,
          buttonStyle = NoButton,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        HedvigTextButton(
          text = stringResource(Res.string.general_close_button),
          onClick = popBackstack,
          buttonSize = Large,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }

    is ChooseInsuranceForMileageUiState.Success -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarText = "",
        topAppBarActions = {
          IconButton(
            modifier = Modifier.size(24.dp),
            onClick = popBackstack,
            content = {
              Icon(
                imageVector = HedvigIcons.Close,
                contentDescription = stringResource(Res.string.general_close_button),
              )
            },
          )
        },
      ) {
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = stringResource(Res.string.CHANGE_MILEAGE_OPTION_TITLE),
          style = HedvigTheme.typography.headlineMedium,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        HedvigText(
          text = stringResource(Res.string.TIER_FLOW_SELECT_INSURANCE_SUBTITLE),
          style = HedvigTheme.typography.headlineMedium.copy(
            lineBreak = LineBreak.Heading,
            color = HedvigTheme.colorScheme.textSecondary,
          ),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        RadioGroup(
          options = uiState.contracts.map { contract ->
            RadioOption(
              id = RadioOptionId(contract.id),
              text = contract.displayName,
              label = contract.exposureDisplayName,
            )
          },
          selectedOption = uiState.selectedContractId?.let { RadioOptionId(it) },
          onRadioOptionSelected = { selectInsurance(it.id) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          enabled = uiState.selectedContractId != null,
          onClick = onContinue,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceForMileageScreen(
  @PreviewParameter(ChooseInsuranceForMileageUiStateProvider::class) uiState: ChooseInsuranceForMileageUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChooseInsuranceForMileageScreen(uiState, {}, {}, {}, {}, {})
    }
  }
}

private class ChooseInsuranceForMileageUiStateProvider :
  CollectionPreviewParameterProvider<ChooseInsuranceForMileageUiState>(
    listOf(
      ChooseInsuranceForMileageUiState.Success(
        contracts = listOf(
          MileageContract("1", "Bilförsäkring Helförsäkring", "ABC 123 • Volvo XC40", ContractGroup.CAR),
          MileageContract("2", "Bilförsäkring Halvförsäkring", "XYZ 789 • Saab 9-3", ContractGroup.CAR),
        ),
        selectedContractId = "2",
      ),
      ChooseInsuranceForMileageUiState.Loading,
      ChooseInsuranceForMileageUiState.Failure,
      ChooseInsuranceForMileageUiState.NoEligibleInsurances,
    ),
  )
