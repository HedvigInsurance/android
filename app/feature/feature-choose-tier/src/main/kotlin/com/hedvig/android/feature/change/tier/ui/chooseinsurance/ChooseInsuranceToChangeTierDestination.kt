package com.hedvig.android.feature.change.tier.ui.chooseinsurance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.data.CustomisableInsurance
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import hedvig.resources.R

@Composable
internal fun ChooseInsuranceToChangeTierDestination(
  viewModel: ChooseInsuranceViewModel,
  navigateUp: () -> Unit,
  navigateToNextStep: (params: InsuranceCustomizationParameters) -> Unit,
) {
  val uiState: ChooseInsuranceUiState by viewModel.uiState.collectAsStateWithLifecycle()

  ChooseInsuranceScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(ChooseInsuranceToCustomizeEvent.RetryLoadData) },
    fetchTerminationStep = { viewModel.emit(ChooseInsuranceToCustomizeEvent.SubmitSelectedInsuranceToTerminate(it)) },
    selectInsurance = { id -> viewModel.emit(ChooseInsuranceToCustomizeEvent.SelectInsurance(id)) },
    navigateToNextStep = { params ->
      viewModel.emit(ChooseInsuranceToCustomizeEvent.ClearTerminationStep)
      navigateToNextStep(params)
    }
  )
}

@Composable
private fun ChooseInsuranceScreen(
  uiState: ChooseInsuranceUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  fetchTerminationStep: (insurance: CustomisableInsurance) -> Unit,
  selectInsurance: (insuranceId: String) -> Unit,
  navigateToNextStep: (params: InsuranceCustomizationParameters) -> Unit,
) {
  when (uiState) {
    ChooseInsuranceUiState.NotAllowed -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = stringResource(R.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
          iconStyle = INFO,
          buttonStyle = NoButton,
          description = null,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        HedvigTextButton(
          stringResource(R.string.general_close_button),
          onClick = navigateUp,
          buttonSize = Large,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
      }
    }

    ChooseInsuranceUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is ChooseInsuranceUiState.Loading -> {
      LaunchedEffect(uiState.paramsToNavigateToNextStep) {
        if (uiState.paramsToNavigateToNextStep != null) {
          navigateToNextStep(uiState.paramsToNavigateToNextStep)
        }
      }
      LoadingScreen(uiState)
    }

    is ChooseInsuranceUiState.Success -> {
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
          text = stringResource(R.string.TIER_FLOW_TITLE),
          style = HedvigTheme.typography.headlineMedium,
          modifier = Modifier.padding(horizontal = 16.dp),
        )

        HedvigText(
          style = HedvigTheme.typography.headlineMedium.copy(
            lineBreak = LineBreak.Heading,
            color = HedvigTheme.colorScheme.textSecondary,
          ),
          text = stringResource(R.string.TIER_FLOW_SELECT_INSURANCE_SUBTITLE),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(
          visible = uiState.changeTierIntentFailedToLoad,
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
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          onClick = {
            uiState.selectedInsurance?.let { selectedInsurance ->
              fetchTerminationStep(selectedInsurance)
            }
          },
        )

        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun LoadingScreen(uiState: ChooseInsuranceUiState.Loading) {
  if (uiState.paramsToNavigateToNextStep == null) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(R.string.TIER_FLOW_PROCESSING),
    )
  } else {
    HedvigFullScreenCenterAlignedProgress()
  }
}

private fun List<CustomisableInsurance>.toListOfDataWithLabel(
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
private fun PreviewChooseInsuranceScreen(
  @PreviewParameter(
    ChooseInsuranceUiStateProvider::class,
  ) uiState: ChooseInsuranceUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChooseInsuranceScreen(
        uiState,
        {},
        {},
        {},
        {},
        {}
      )
    }
  }
}

private class ChooseInsuranceUiStateProvider :
  CollectionPreviewParameterProvider<ChooseInsuranceUiState>(
    listOf(
      ChooseInsuranceUiState.Success(
        insuranceList = listOf(
          CustomisableInsurance(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          CustomisableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        selectedInsurance = null,
        changeTierIntentFailedToLoad = false,
      ),
      ChooseInsuranceUiState.Success(
        insuranceList = listOf(
          CustomisableInsurance(
            id = "1",
            displayName = "HomeownerInsurance",
            contractExposure = "Opulullegatan 19",
            contractGroup = ContractGroup.HOUSE,
          ),
          CustomisableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
          ),
        ),
        selectedInsurance = CustomisableInsurance(
          id = "3",
          displayName = "Tenant Insurance",
          contractExposure = "Bullegatan 23",
          contractGroup = ContractGroup.HOUSE,
        ),
        changeTierIntentFailedToLoad = true,
      ),
      ChooseInsuranceUiState.Failure,
      ChooseInsuranceUiState.Loading(),
      ChooseInsuranceUiState.NotAllowed,
    ),
  )
