package com.hedvig.android.feature.editcoinsured.ui.triage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import hedvig.resources.Res
import hedvig.resources.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_COINSURED
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditCoInsuredTriageDestination(
  viewModel: EditCoInsuredTriageViewModel,
  navigateUp: () -> Unit,
  navigateToAddMissingInfo: (String) -> Unit,
  navigateToAddOrRemoveCoInsured: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  EditCoInsuredTriageScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = {
      viewModel.emit(EditCoInsuredTriageEvent.Reload)
    },
    submitSelectedInsurance = {
      viewModel.emit(EditCoInsuredTriageEvent.OnContinueWithSelected)
    },
    selectInsurance = {
      viewModel.emit(EditCoInsuredTriageEvent.SelectInsurance(it))
    },
    clearNavigation = {
      viewModel.emit(EditCoInsuredTriageEvent.ClearNavigation)
    },
    navigateToAddMissingInfo = navigateToAddMissingInfo,
    navigateToAddOrRemoveCoInsured = navigateToAddOrRemoveCoInsured,
  )
}

@Composable
private fun EditCoInsuredTriageScreen(
  uiState: EditCoInsuredTriageUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  navigateToAddMissingInfo: (String) -> Unit,
  navigateToAddOrRemoveCoInsured: (String) -> Unit,
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
      LaunchedEffect(uiState.idToNavigateToAddOrRemoveCoInsured) {
        if (uiState.idToNavigateToAddOrRemoveCoInsured != null) {
          clearNavigation()
          navigateToAddOrRemoveCoInsured(uiState.idToNavigateToAddOrRemoveCoInsured)
        }
      }
      LaunchedEffect(uiState.idToNavigateToAddMissingInfo) {
        if (uiState.idToNavigateToAddMissingInfo != null) {
          clearNavigation()
          navigateToAddMissingInfo(uiState.idToNavigateToAddMissingInfo)
        }
      }
      if (uiState.idToNavigateToAddMissingInfo == null && uiState.idToNavigateToAddOrRemoveCoInsured == null) {
        SuccessScreen(
          uiState = uiState,
          navigateUp = navigateUp,
          submitSelectedInsurance = submitSelectedInsurance,
          selectInsurance = selectInsurance,
        )
      } else {
        HedvigFullScreenCenterAlignedProgress()
      }
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
            contentDescription = stringResource(Res.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = stringResource(Res.string.HC_QUICK_ACTIONS_EDIT_COINSURED),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(Res.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    RadioGroup(
      options = uiState.list.map { insurance ->
        RadioOption(
          id = RadioOptionId(insurance.id),
          text = insurance.displayName,
          label = insurance.exposureName,
        )
      },
      selectedOption = uiState.selected?.id?.let { RadioOptionId(it) },
      onRadioOptionSelected = { selectInsurance(it.id) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(Res.string.general_continue_button),
      enabled = uiState.selected != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = submitSelectedInsurance,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewEditCoInsuredTriageScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EditCoInsuredTriageScreen(
        uiState = Success(
          list = listOf(
            InsuranceForEditOrAddCoInsured(
              id = "1",
              displayName = "Home insurance",
              exposureName = "Lulanden 85H, 71220",
              destination = EditCoInsuredDestination.MISSING_INFO,
            ),
            InsuranceForEditOrAddCoInsured(
              id = "2",
              displayName = "Home insurance",
              exposureName = "Drottninggatan 1, 11111",
              destination = EditCoInsuredDestination.MISSING_INFO,
            ),
          ),
          selected = null,
          null,
          null,
        ),
        navigateUp = {},
        reload = {},
        navigateToAddMissingInfo = {},
        navigateToAddOrRemoveCoInsured = {},
        submitSelectedInsurance = {},
        selectInsurance = {},
        clearNavigation = {},
      )
    }
  }
}
