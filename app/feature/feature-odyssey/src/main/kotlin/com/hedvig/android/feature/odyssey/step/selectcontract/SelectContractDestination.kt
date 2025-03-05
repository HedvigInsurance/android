package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun SelectContractDestination(
  viewModel: SelectContractViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectContractScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocation = {
      viewModel.emit(SelectContractEvent.SelectContractOption(it))
    },
    submitLocation = {
      viewModel.emit(SelectContractEvent.Submit)
    },
    showedError = {
      viewModel.emit(SelectContractEvent.ShowedError)
    },
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    reload = {
      viewModel.emit(SelectContractEvent.Reload)
    },
    navigateToNextStep = navigateToNextStep,
  )
}

@Composable
private fun SelectContractScreen(
  uiState: SelectContractUiState,
  windowSizeClass: WindowSizeClass,
  selectLocation: (String) -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  when (val state = uiState) {
    is SelectContractUiState.Success -> {
      val claimFlowStep = state.nextStep
      LaunchedEffect(claimFlowStep) {
        if (claimFlowStep != null) {
          navigateToNextStep(claimFlowStep)
        }
      }
      SelectContractSuccessScreen(
        uiState = state,
        windowSizeClass = windowSizeClass,
        selectLocation = selectLocation,
        submitLocation = submitLocation,
        showedError = showedError,
        navigateUp = navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    SelectContractUiState.Error -> HedvigErrorSection(
      onButtonClick = reload,
      modifier = Modifier.fillMaxSize(),
    )
    SelectContractUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
  }
}

@Composable
private fun SelectContractSuccessScreen(
  uiState: SelectContractUiState.Success,
  windowSizeClass: WindowSizeClass,
  selectLocation: (String) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.error,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.CLAIM_TRIAGING_ABOUT_TITILE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    RadioGroup(
      radioGroupSize = RadioGroupDefaults.RadioGroupSize.Medium,
      radioGroupStyle = RadioGroupDefaults.RadioGroupStyle.Vertical.Label(
        dataList = uiState.contractOptions.map { option ->
          RadioOptionGroupData.RadioOptionGroupDataWithLabel(
            RadioOptionData(
              id = option.id,
              optionText = option.displayName,
              chosenState = if (option == uiState.selectedContract) ChosenState.Chosen else ChosenState.NotChosen,
            ),
            labelText = option.description,
          )
        },
      ),
      onOptionClick = { id ->
        selectLocation(id)
      },
      modifier = sideSpacingModifier.fillMaxWidth(),
      groupLockedState = NotLocked,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitLocation,
      isLoading = uiState.isButtonLoading,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewLocationScreen(
  @PreviewParameter(UiStatePreviewProvider::class) uiState: SelectContractUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectContractScreen(
        uiState = uiState,
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        selectLocation = {},
        submitLocation = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
        navigateToNextStep = {},
        reload = {},
      )
    }
  }
}

private class UiStatePreviewProvider : CollectionPreviewParameterProvider<SelectContractUiState>(
  listOf(
    SelectContractUiState.Success(
      contractOptions = List(3) {
        ContractOptionForSelection(
          "#$it",
          "Location #$it",
          description = "Bullegatan Bullegatan",
        )
      },
      selectedContract = ContractOptionForSelection(
        "#1",
        "Location #1",
        description = "Bullegatan Bullegatan",
      ),
    ),
    SelectContractUiState.Success(
      contractOptions = List(3) {
        ContractOptionForSelection(
          "#$it",
          "Location #$it",
          description = "Bullegatan Bullegatan",
        )
      },
      isButtonLoading = true,
      selectedContract = ContractOptionForSelection(
        "#1",
        "Location #1",
        description = "Bullegatan Bullegatan",
      ),
    ),
    SelectContractUiState.Success(
      contractOptions = List(3) {
        ContractOptionForSelection(
          "#$it",
          "Location #$it",
          description = "Bullegatan Bullegatan",
        )
      },
      error = true,
      selectedContract = ContractOptionForSelection(
        "#1",
        "Location #1",
        description = "Bullegatan Bullegatan",
      ),
    ),
    SelectContractUiState.Error,
    SelectContractUiState.Loading,
  ),
)
