package com.hedvig.android.feature.movingflow.ui.selectcontract

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.fragment.MoveIntentFragment

@Composable
internal fun SelectContractDestination(
  viewModel: SelectContractViewModel,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  goToChat: () -> Unit,
  onNavigateToNextStep: (moveIntentId: String, popUpDestination: Boolean) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val uiStateValue = uiState as? SelectContractState.NotEmpty ?: return@LaunchedEffect
    if (uiStateValue.navigateToHousingType) {
      val shouldPopUp = uiStateValue.intent.currentHomeAddresses.size < 2
      viewModel.emit(SelectContractEvent.ClearNavigation)
      onNavigateToNextStep(uiStateValue.intent.id, shouldPopUp)
    }
  }
  SelectContractScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    closeFlow = exitFlow,
    reload = { viewModel.emit(SelectContractEvent.RetryLoadData) },
    selectInsurance = { address -> viewModel.emit(SelectContractEvent.SelectContract(address)) },
    submitChoice = { viewModel.emit(SelectContractEvent.SubmitContract) },
    goToChat = goToChat,
  )
}

@Composable
private fun SelectContractScreen(
  uiState: SelectContractState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  goToChat: () -> Unit,
  closeFlow: () -> Unit,
  submitChoice: () -> Unit,
  selectInsurance: (address: MoveIntentFragment.CurrentHomeAddress) -> Unit,
) {
  when (uiState) {
    is SelectContractState.Error -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          buttonText = when (uiState) {
            is SelectContractState.Error.UserPresentable -> stringResource(R.string.open_chat)
            else -> stringResource(R.string.GENERAL_RETRY)
          },
          onButtonClick = when (uiState) {
            is SelectContractState.Error.UserPresentable -> goToChat
            else -> reload
          },
          title =  when (uiState) {
            is SelectContractState.Error.UserPresentable -> stringResource(R.string.GENERAL_CONTACT_US_TITLE)
            else ->  stringResource(R.string.something_went_wrong)
          },
          subTitle = when (uiState) {
            is SelectContractState.Error.UserPresentable -> uiState.message
            else -> stringResource(R.string.GENERAL_ERROR_BODY)
          },
          modifier = Modifier.fillMaxWidth().weight(1f),
        )
      }
    }

    is SelectContractState.Loading, is SelectContractState.NotEmpty.Redirecting -> HedvigFullScreenCenterAlignedProgress()

    is SelectContractState.NotEmpty.Content -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarText = "",
        topAppBarActions = {
          IconButton(
            modifier = Modifier.size(24.dp),
            onClick = { closeFlow() },
            content = {
              Icon(
                imageVector = HedvigIcons.Close,
                contentDescription = stringResource(R.string.general_close_button),
              )
            },
          )
        },
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowHeading(
          title = stringResource(id = R.string.insurance_details_change_address_button),
          stringResource(id = R.string.MOVING_FLOW_BODY),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        val radioOptionData = uiState.intent.currentHomeAddresses
          .toListOfDataWithLabel(uiState.selectedAddress?.id)
        RadioGroup(
          onOptionClick = { insuranceId ->
            val address = uiState.intent.currentHomeAddresses.first { it.id == insuranceId }
            selectInsurance(address)
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          radioGroupSize = RadioGroupSize.Medium,
          radioGroupStyle = RadioGroupStyle.Vertical.Label(radioOptionData),
        )
        Spacer(Modifier.height(12.dp))

        HedvigButton(
          stringResource(id = R.string.general_continue_button),
          enabled = uiState.selectedAddress != null,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          onClick = {
            submitChoice()
          },
          isLoading = uiState.buttonLoading,
        )

        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

private fun List<MoveIntentFragment.CurrentHomeAddress>.toListOfDataWithLabel(
  selectedInsuranceId: String?,
): List<RadioOptionGroupDataWithLabel> {
  return this.map { i ->
    RadioOptionGroupDataWithLabel(
      RadioOptionData(
        id = i.id,
        optionText = i.displayTitle,
        chosenState = if (selectedInsuranceId == i.id) Chosen else NotChosen,
      ),
      labelText = i.displaySubtitle ?: " ",
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    ChooseInsuranceUiStateProvider::class,
  ) uiState: SelectContractState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectContractScreen(
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

val previewMovingIntent = MoveIntentV2CreateMutation.Data.MoveIntentCreate.MoveIntent(
  __typename = "",
  id = "id",
  maxHouseNumberCoInsured = null,
  maxHouseSquareMeters = null,
  maxApartmentNumberCoInsured = null,
  maxApartmentSquareMeters = null,
  isApartmentAvailableforStudent = null,
  extraBuildingTypes = listOf(),
  currentHomeAddresses = listOf(
    MoveIntentV2CreateMutation.Data.MoveIntentCreate.MoveIntent.CurrentHomeAddress(
      id = "id1",
      oldAddressCoverageDurationDays = 30,
      suggestedNumberCoInsured = 2,
      displaySubtitle = "Subtitle",
      displayTitle = "Title",
      minMovingDate = LocalDate(2024, 11, 5),
      maxMovingDate = LocalDate(2024, 12, 31),
    ),
    MoveIntentV2CreateMutation.Data.MoveIntentCreate.MoveIntent.CurrentHomeAddress(
      id = "id2",
      oldAddressCoverageDurationDays = 30,
      suggestedNumberCoInsured = 2,
      displaySubtitle = "Subtitle",
      displayTitle = "Title",
      minMovingDate = LocalDate(2024, 11, 5),
      maxMovingDate = LocalDate(2024, 12, 31),
    ),
  ),
)

private class ChooseInsuranceUiStateProvider :
  CollectionPreviewParameterProvider<SelectContractState>(
    listOf(
      SelectContractState.Loading,
      SelectContractState.Error.UserPresentable("Presentable error explanation"),
      SelectContractState.Error.GenericError(ErrorMessage()),
      SelectContractState.NotEmpty.Redirecting(
        intent = previewMovingIntent,
        selectedAddress = previewMovingIntent.currentHomeAddresses[0],
        navigateToHousingType = false,
      ),
      SelectContractState.NotEmpty.Content(
        intent = previewMovingIntent,
        selectedAddress = null,
        navigateToHousingType = false,
        buttonLoading = false,
      ),
      SelectContractState.NotEmpty.Content(
        intent = previewMovingIntent,
        selectedAddress = previewMovingIntent.currentHomeAddresses[1],
        navigateToHousingType = false,
        buttonLoading = false,
      ),
    ),
  )
