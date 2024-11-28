package com.hedvig.android.feature.changeaddress.destination.selecthousingtype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.data.HousingType.APARTMENT_OWN
import com.hedvig.android.feature.changeaddress.data.HousingType.APARTMENT_RENT
import com.hedvig.android.feature.changeaddress.data.HousingType.VILLA
import com.hedvig.android.feature.changeaddress.data.MoveIntent
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.displayNameResource
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import kotlinx.datetime.LocalDate

@Composable
internal fun SelectHousingTypeDestination(
  viewModel: SelectHousingTypeViewModel,
  navigateUp: () -> Unit,
  navigateToEnterNewAddressDestination: (SelectHousingTypeParameters) -> Unit,
) {
  val uiState: SelectHousingTypeUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val state = uiState
  if (state is SelectHousingTypeUiState.Content) {
    LaunchedEffect(state.navigationParameters) {
      val params = state.navigationParameters
      if (params != null) {
        viewModel.emit(SelectHousingTypeEvent.ClearNavigationParameters)
        navigateToEnterNewAddressDestination(params)
      }
    }
  }
  ChangeAddressSelectHousingTypeScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onHousingTypeSelected = { type ->
      viewModel.emit(SelectHousingTypeEvent.SelectHousingType(type))
    },
    onHousingTypeSubmitted = {
      viewModel.emit(SelectHousingTypeEvent.SubmitHousingType)
    },
    onHousingTypeErrorDialogDismissed = {
      viewModel.emit(SelectHousingTypeEvent.DismissHousingTypeErrorDialog)
    },
    onErrorDialogDismissed = {
      viewModel.emit(SelectHousingTypeEvent.DismissErrorDialog)
    },
  )
}

@Composable
private fun ChangeAddressSelectHousingTypeScreen(
  uiState: SelectHousingTypeUiState,
  navigateUp: () -> Unit,
  onHousingTypeSelected: (HousingType) -> Unit,
  onHousingTypeSubmitted: () -> Unit,
  onHousingTypeErrorDialogDismissed: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
) {
  when (uiState) {
    is SelectHousingTypeUiState.Content -> SelectHousingTypeContentScreen(
      uiState = uiState,
      navigateUp = navigateUp,
      onHousingTypeSelected = onHousingTypeSelected,
      onHousingTypeSubmitted = onHousingTypeSubmitted,
      onHousingTypeErrorDialogDismissed = onHousingTypeErrorDialogDismissed,
    )
    is SelectHousingTypeUiState.Error -> ErrorDialog(
      title = stringResource(hedvig.resources.R.string.general_error),
      message = uiState.errorMessage,
      onDismiss = { onErrorDialogDismissed() },
    )
    SelectHousingTypeUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
  }
}

@Composable
private fun SelectHousingTypeContentScreen(
  uiState: SelectHousingTypeUiState.Content,
  navigateUp: () -> Unit,
  onHousingTypeSelected: (HousingType) -> Unit,
  onHousingTypeSubmitted: () -> Unit,
  onHousingTypeErrorDialogDismissed: () -> Unit,
) {
  uiState.errorMessageRes?.let {
    ErrorDialog(
      title = stringResource(hedvig.resources.R.string.general_error),
      message = stringResource(it),
      onDismiss = { onHousingTypeErrorDialogDismissed() },
    )
  }
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_SELECT_HOUSING_TYPE_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.weight(1f))
    Spacer(modifier = Modifier.height(16.dp))
    RadioButton(APARTMENT_OWN, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(8.dp))
    RadioButton(APARTMENT_RENT, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(8.dp))
    RadioButton(VILLA, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(16.dp))
    if (uiState.oldHomeInsuranceDuration != null) {
      VectorInfoCard(
        text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT, 30),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
    HedvigContainedButton(
      text = stringResource(id = hedvig.resources.R.string.general_continue_button),
      onClick = onHousingTypeSubmitted,
      isLoading = uiState.isButtonLoading,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RadioButton(
  housingType: HousingType,
  selectedHousingType: HousingType?,
  selectHousingType: (HousingType) -> Unit,
) {
  HedvigCard(
    onClick = { selectHousingType(housingType) },
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(housingType.displayNameResource()),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f),
      )
      RadioButton(
        selected = selectedHousingType == housingType,
        onClick = {
          selectHousingType(housingType)
        },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeAddressSelectHousingTypeScreen(
  @PreviewParameter(SelectHousingTypeUiStateProvider::class) state: SelectHousingTypeUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressSelectHousingTypeScreen(
        state,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class SelectHousingTypeUiStateProvider : CollectionPreviewParameterProvider<SelectHousingTypeUiState>(
  listOf(
    SelectHousingTypeUiState.Error("Unknown"),
    SelectHousingTypeUiState.Loading,
    SelectHousingTypeUiState.Content(
      housingType = ValidatedInput(APARTMENT_OWN),
      moveIntent = MoveIntent(
        id = MoveIntentId("id"),
        movingDateRange = LocalDate(2024, 11, 5)..LocalDate(2024, 12, 31),
        maxHouseNumberCoInsured = null,
        maxHouseSquareMeters = null,
        maxApartmentNumberCoInsured = null,
        maxApartmentSquareMeters = null,
        isApartmentAvailableforStudent = null,
        extraBuildingTypes = listOf(),
        currentHomeAddresses = listOf(),
        suggestedNumberInsured = 2,
        oldAddressCoverageDurationDays = 30,
      ),
      oldHomeInsuranceDuration = 30,
      navigationParameters = null,
    ),
    SelectHousingTypeUiState.Content(
      housingType = ValidatedInput(APARTMENT_OWN),
      moveIntent = MoveIntent(
        id = MoveIntentId("id"),
        movingDateRange = LocalDate(2024, 11, 5)..LocalDate(2024, 12, 31),
        maxHouseNumberCoInsured = null,
        maxHouseSquareMeters = null,
        maxApartmentNumberCoInsured = null,
        maxApartmentSquareMeters = null,
        isApartmentAvailableforStudent = null,
        extraBuildingTypes = listOf(),
        currentHomeAddresses = listOf(),
        suggestedNumberInsured = 2,
        oldAddressCoverageDurationDays = null,
      ),
      oldHomeInsuranceDuration = null,
      navigationParameters = null,
    ),
  ),
)
