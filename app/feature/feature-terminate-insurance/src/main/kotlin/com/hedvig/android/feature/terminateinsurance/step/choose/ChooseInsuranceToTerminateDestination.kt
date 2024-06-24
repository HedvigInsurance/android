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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun ChooseInsuranceToTerminateDestination(
  viewModel: ChooseInsuranceToTerminateViewModel,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
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
    openChat = openChat,
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
      ) {
        Text(
          style = MaterialTheme.typography.headlineSmall.copy(
            lineBreak = LineBreak.Heading,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
          text = stringResource(id = R.string.TERMINATION_FLOW_CHOOSE_CONTRACT_SUBTITLE),
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
            WarningTextWithIcon(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(),
              text = stringResource(R.string.something_went_wrong),
            )
            Spacer(Modifier.height(16.dp))
          }
        }
        val radioOptionData = uiState.insuranceList.toRadioOptionDataList(uiState.selectedInsurance?.id)
        com.hedvig.android.design.system.hedvig.HedvigTheme {
          // todo: where do we apply the theme now that we're still on both old and new theme?
          RadioGroup(
            data = radioOptionData,
            onOptionClick = { insuranceId -> selectInsurance(insuranceId) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            radioGroupSize = RadioGroupSize.Medium,
            radioGroupStyle = RadioGroupStyle.Vertical.Label,
          )
        }
        Spacer(Modifier.height(12.dp))
        HedvigContainedButton(
          stringResource(id = R.string.general_continue_button),
          enabled = uiState.selectedInsurance != null,
          modifier = Modifier.padding(horizontal = 16.dp),
          colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
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

private fun List<TerminatableInsurance>.toRadioOptionDataList(selectedInsuranceId: String?): List<RadioOptionData> {
  val result = mutableListOf<RadioOptionData>()
  for (i in this) {
    result.add(
      RadioOptionData(
        id = i.id,
        optionText = i.displayName,
        chosenState = if (selectedInsuranceId == i.id) Chosen else NotChosen,
        labelText = i.contractExposure,
      ),
    )
  }
  return result
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    ChooseInsuranceToTerminateStepUiStateProvider::class,
  ) uiState: ChooseInsuranceToTerminateStepUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
            activateFrom = LocalDate(2024, 6, 27),
          ),
          TerminatableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
            activateFrom = LocalDate(2024, 6, 27),
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
            activateFrom = LocalDate(2024, 6, 27),
          ),
          TerminatableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
            activateFrom = LocalDate(2024, 6, 27),
          ),
        ),
        selectedInsurance = TerminatableInsurance(
          id = "3",
          displayName = "Tenant Insurance",
          contractExposure = "Bullegatan 23",
          contractGroup = ContractGroup.HOUSE,
          activateFrom = LocalDate(2024, 6, 27),
        ),
        isNavigationStepLoading = true,
        navigationStepFailedToLoad = true,
      ),
      ChooseInsuranceToTerminateStepUiState.Failure,
      ChooseInsuranceToTerminateStepUiState.NotAllowed,
      ChooseInsuranceToTerminateStepUiState.Loading,
    ),
  )
