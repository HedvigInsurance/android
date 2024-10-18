package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.Content
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError.GenericError
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError.UserPresentable
import hedvig.resources.R

@Composable
internal fun StartDestination(
  viewModel: StartViewModel,
  navigateUp: () -> Unit,
  onNavigateToNextStep: (String) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState is Content && uiState.initiatedMovingFlowId != null) {
    LaunchedEffect(uiState.initiatedMovingFlowId) {
      viewModel.emit(StartEvent.NavigatedToNextStep)
      onNavigateToNextStep(uiState.initiatedMovingFlowId)
    }
  }
  StartScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onDismissStartError = { viewModel.emit(StartEvent.DismissStartError) },
    onSelectHousingType = { viewModel.emit(StartEvent.SelectHousingType(it)) },
    onSubmitHousingType = { viewModel.emit(StartEvent.SubmitHousingType) },
  )
}

@Composable
private fun StartScreen(
  uiState: StartUiState,
  navigateUp: () -> Unit,
  onDismissStartError: () -> Unit,
  onSelectHousingType: (HousingType) -> Unit,
  onSubmitHousingType: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigScaffold(navigateUp, modifier) {
    when (uiState) {
      is StartError -> {
        HedvigErrorSection(
          onButtonClick = onDismissStartError,
          title = when (uiState) {
            is GenericError -> stringResource(R.string.something_went_wrong)
            is UserPresentable -> uiState.message
          },
        )
      }

      is Content -> {
        HedvigText("START Select a housing type")
        Spacer(Modifier.weight(1f))
        RadioGroup(
          radioGroupStyle = RadioGroupStyle.Vertical.Default(
            uiState.possibleHousingTypes.map {
              RadioOptionGroupDataSimple(
                RadioOptionData(
                  id = it.name,
                  optionText = stringResource(it.stringResource()),
                  chosenState = if (it == uiState.selectedHousingType) ChosenState.Chosen else ChosenState.NotChosen,
                  lockedState = NotLocked,
                ),
              )
            },
          ),
          onOptionClick = {
            onSelectHousingType(HousingType.valueOf(it))
          },
        )
        HedvigButton(
          "SubmiT",
          onSubmitHousingType,
          enabled = uiState.submittingHousingType == null,
        )
      }
    }
  }
}

internal fun HousingType.stringResource() = when (this) {
  HousingType.ApartmentRent -> R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.ApartmentOwn -> R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.Villa -> R.string.CHANGE_ADDRESS_VILLA_LABEL
}
