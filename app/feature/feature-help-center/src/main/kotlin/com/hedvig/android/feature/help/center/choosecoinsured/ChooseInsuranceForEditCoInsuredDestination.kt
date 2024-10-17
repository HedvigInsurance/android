package com.hedvig.android.feature.help.center.choosecoinsured

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
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Failure
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Loading
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Success
import com.hedvig.android.feature.help.center.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import hedvig.resources.R

@Composable
internal fun ChooseInsuranceForEditCoInsuredDestination(
  viewModel: ChooseInsuranceForEditCoInsuredViewModel,
  navigateToNextStep: (QuickLinkDestination.OuterDestination) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseInsuranceForEditCoInsuredScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = {
      viewModel.emit(ChooseInsuranceForEditCoInsuredEvent.Reload)
    },
    navigateToNextStep = navigateToNextStep,
    submitSelectedInsurance = {
      viewModel.emit(ChooseInsuranceForEditCoInsuredEvent.OnContinueWithSelected)
    },
    selectInsurance = {
      viewModel.emit(ChooseInsuranceForEditCoInsuredEvent.SelectInsurance(it))
    },
    clearNavigation = {
      viewModel.emit(ChooseInsuranceForEditCoInsuredEvent.ClearNavigation)
    },
  )
}

@Composable
private fun ChooseInsuranceForEditCoInsuredScreen(
  uiState: ChooseInsuranceForEditCoInsuredUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  navigateToNextStep: (QuickLinkDestination.OuterDestination) -> Unit,
  submitSelectedInsurance: () -> Unit,
  selectInsurance: (id: String) -> Unit,
  clearNavigation: () -> Unit,
) {
  when (uiState) {
    Failure -> HedvigScaffold(
      navigateUp = navigateUp,
    ) {
      HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()
    is Success -> {
      LaunchedEffect(uiState.destinationToNavigateToNextStep) {
        if (uiState.destinationToNavigateToNextStep != null) {
          clearNavigation()
          uiState.selected?.let {
            navigateToNextStep(it.quickLinkDestination)
          }
        }
      }
      SuccessScreen(
        uiState = uiState,
        navigateUp = navigateUp,
        submitSelectedInsurance = submitSelectedInsurance,
        selectInsurance = selectInsurance,
      )
    }
  }
}

@Composable
private fun SuccessScreen(
  uiState: Success,
  navigateUp: () -> Unit,
  submitSelectedInsurance: () -> Unit,
  selectInsurance: (id: String) -> Unit,
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
    HedvigText(
      text = stringResource(R.string.HC_QUICK_ACTIONS_EDIT_COINSURED),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    val radioOptionData = mapToListOfDataWithLabel(uiState.list, uiState.selected)
    RadioGroup(
      onOptionClick = { insuranceId -> selectInsurance(insuranceId) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      radioGroupSize = RadioGroupSize.Medium,
      radioGroupStyle = RadioGroupStyle.Vertical.Label(radioOptionData),
    )
    Spacer(Modifier.height(12.dp))
    HedvigButton(
      stringResource(id = R.string.general_continue_button),
      enabled = uiState.selected != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = submitSelectedInsurance,
    )
    Spacer(Modifier.height(16.dp))
  }
}

private fun mapToListOfDataWithLabel(
  list: List<InsuranceForEditOrAddCoInsured>,
  selectedInsurance: InsuranceForEditOrAddCoInsured?,
): List<RadioOptionGroupDataWithLabel> {
  return buildList {
    list.forEachIndexed { index, insurance ->
      add(
        RadioOptionGroupDataWithLabel(
          RadioOptionData(
            id = insurance.id,
            optionText = insurance.displayName,
            chosenState = if (selectedInsurance == insurance) Chosen else NotChosen,
          ),
          labelText = insurance.exposureName,
        ),
      )
    }
  }
}
