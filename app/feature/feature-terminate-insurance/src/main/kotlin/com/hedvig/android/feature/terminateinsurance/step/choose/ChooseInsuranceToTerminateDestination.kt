package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun ChooseInsuranceToTerminateDestination(
  viewModel: ChooseInsuranceToTerminateViewModel,
  navigateUp: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep, terminatableInsurance: TerminatableInsurance) -> Unit,
) {
  val uiState: ChooseInsuranceToTerminateStepUiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val uiStateValue = uiState as? ChooseInsuranceToTerminateStepUiState.Success ?: return@LaunchedEffect
    if (uiStateValue.nextStepWithInsurance != null) {
      viewModel.emit(ChooseInsuranceToTerminateEvent.ClearTerminationStep)
      navigateToNextStep(uiStateValue.nextStepWithInsurance.first, uiStateValue.nextStepWithInsurance.second)
    }
  }
  ChooseInsuranceToTerminateScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    openChat = onNavigateToNewConversation,
    closeTerminationFlow = closeTerminationFlow,
    reload = { viewModel.emit(ChooseInsuranceToTerminateEvent.RetryLoadData) },
    fetchTerminationStep = { viewModel.emit(ChooseInsuranceToTerminateEvent.SubmitSelectedInsuranceToTerminate(it)) },
    selectInsurance = { id -> viewModel.emit(ChooseInsuranceToTerminateEvent.SelectInsurance(id)) },
  )
}

@Composable
private fun ChooseInsuranceToTerminateScreen(
  uiState: ChooseInsuranceToTerminateStepUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  openChat: () -> Unit,
  closeTerminationFlow: () -> Unit,
  fetchTerminationStep: (insurance: TerminatableInsurance) -> Unit,
  selectInsurance: (insuranceId: String) -> Unit,
) {
  when (uiState) {
    ChooseInsuranceToTerminateStepUiState.NotAllowed -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          onButtonClick = openChat,
          modifier = Modifier.weight(1f),
          title = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE_TITLE),
          subTitle = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE),
          buttonText = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE_BUTTON),
        )
      }
    }

    ChooseInsuranceToTerminateStepUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    ChooseInsuranceToTerminateStepUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    is ChooseInsuranceToTerminateStepUiState.Success -> {
      TerminationScaffold(
        navigateUp = navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        textForInfoIcon = stringResource(id = R.string.TERMINATION_FLOW_CANCEL_INFO_TEXT),
      ) { title ->
        FlowHeading(
          title,
          stringResource(id = R.string.TERMINATION_FLOW_CHOOSE_CONTRACT_SUBTITLE),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(
          visible = uiState.navigationStepFailedToLoad,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Column {
            EmptyState(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(),
              text = stringResource(R.string.something_went_wrong),
              iconStyle = ERROR,
              description = null,
            )
            Spacer(Modifier.height(16.dp))
          }
        }
        val radioOptionData = uiState.insuranceList.toListOfDataWithLabel(uiState.selectedInsurance?.id)
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
          enabled = uiState.selectedInsurance != null,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          onClick = {
            uiState.selectedInsurance?.let { selectedInsurance ->
              fetchTerminationStep(selectedInsurance)
            }
          },
          isLoading = uiState.isNavigationStepLoading,
        )

        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

private fun List<TerminatableInsurance>.toListOfDataWithLabel(
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
    ChooseInsuranceToTerminateStepUiStateProvider::class,
  ) uiState: ChooseInsuranceToTerminateStepUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChooseInsuranceToTerminateScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class ChooseInsuranceToTerminateStepUiStateProvider :
  CollectionPreviewParameterProvider<ChooseInsuranceToTerminateStepUiState>(
    listOf(
      ChooseInsuranceToTerminateStepUiState.Success(
        nextStepWithInsurance = null,
        insuranceList = listOf(
          TerminatableInsurance(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          TerminatableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        selectedInsurance = null,
        isNavigationStepLoading = false,
        navigationStepFailedToLoad = false,
      ),
      ChooseInsuranceToTerminateStepUiState.Success(
        nextStepWithInsurance = null,
        insuranceList = listOf(
          TerminatableInsurance(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          TerminatableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        selectedInsurance = TerminatableInsurance(
          id = "3",
          displayName = "Tenant Insurance",
          contractExposure = "Bullegatan 23",
          contractGroup = ContractGroup.HOUSE,
        ),
        isNavigationStepLoading = true,
        navigationStepFailedToLoad = true,
      ),
      ChooseInsuranceToTerminateStepUiState.Failure,
      ChooseInsuranceToTerminateStepUiState.NotAllowed,
      ChooseInsuranceToTerminateStepUiState.Loading,
    ),
  )
