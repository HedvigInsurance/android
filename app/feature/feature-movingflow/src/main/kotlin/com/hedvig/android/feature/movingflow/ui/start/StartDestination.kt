package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.Vertical.Default
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
          subTitle = when (uiState) {
            is GenericError -> stringResource(R.string.GENERAL_ERROR_BODY)
            is UserPresentable -> null
          },
        )
      }

      is Content -> {
        StartScreen(uiState, onSelectHousingType, onSubmitHousingType, Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun StartScreen(
  uiState: Content,
  onSelectHousingType: (HousingType) -> Unit,
  onSubmitHousingType: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.padding(horizontal = 16.dp)) {
    HedvigText(
      text = stringResource(R.string.insurance_details_change_address_button),
      style = HedvigTheme.typography.bodyMedium,
    )
    HedvigText(
      text = stringResource(R.string.CHANGE_ADDRESS_SELECT_HOUSING_TYPE_TITLE),
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    RadioGroup(
      radioGroupStyle = Default(
        uiState.possibleHousingTypes.map {
          RadioOptionGroupDataSimple(
            RadioOptionData(
              id = it.name,
              optionText = stringResource(it.stringResource()),
              chosenState = if (it == uiState.selectedHousingType) Chosen else NotChosen,
              lockedState = NotLocked,
            ),
          )
        },
      ),
      onOptionClick = {
        onSelectHousingType(HousingType.valueOf(it))
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigNotificationCard(stringResource(R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT), Info)
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onSubmitHousingType,
      enabled = uiState.submittingHousingType == null,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

internal fun HousingType.stringResource() = when (this) {
  HousingType.ApartmentRent -> R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.ApartmentOwn -> R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.Villa -> R.string.CHANGE_ADDRESS_VILLA_LABEL
}
