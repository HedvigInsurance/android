package com.hedvig.android.feature.changeaddress.destination.entervillainfo

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.ui.ChangeAddressSwitch
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import com.hedvig.android.feature.changeaddress.ui.extrabuildings.ExtraBuildingBottomSheet
import com.hedvig.android.feature.changeaddress.ui.extrabuildings.ExtraBuildingContainer
import hedvig.resources.R

@Composable
internal fun ChangeAddressEnterVillaInformationDestination(
  viewModel: ChangeAddressViewModel,
  navigateUp: () -> Unit,
  onNavigateToOfferDestination: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val navigateToOfferScreenAfterHavingReceivedQuotes = uiState.navigateToOfferScreenAfterHavingReceivedQuotes
  LaunchedEffect(navigateToOfferScreenAfterHavingReceivedQuotes) {
    if (navigateToOfferScreenAfterHavingReceivedQuotes) {
      viewModel.onNavigatedToOfferScreenAfterHavingReceivedQuotes()
      onNavigateToOfferDestination()
    }
  }

  var showExtraBuildingsBottomSheet by rememberSaveable { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(true)
  if (showExtraBuildingsBottomSheet) {
    ExtraBuildingBottomSheet(
      extraBuildingTypes = uiState.extraBuildingTypes,
      onDismiss = {
        showExtraBuildingsBottomSheet = false
      },
      onSave = {
        showExtraBuildingsBottomSheet = false
        viewModel.addExtraBuilding(it)
      },
      sheetState = sheetState,
    )
  }

  ChangeAddressEnterVillaInformationScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onYearOfConstructionChanged = viewModel::onYearOfConstructionChanged,
    onAncillaryAreaChanged = viewModel::onAncillaryAreaChanged,
    onNumberOfBathroomsChanged = viewModel::onNumberOfBathroomsChanged,
    onIsSubletSelected = viewModel::onIsSubletChanged,
    onSaveNewAddress = {
      val isInputValid = viewModel.validateHouseInput()
      if (isInputValid) {
        viewModel.onSubmitNewAddress()
      }
    },
    onAddExtraBuildingClicked = {
      showExtraBuildingsBottomSheet = true
    },
    onRemoveExtraBuildingClicked = viewModel::onRemoveExtraBuildingClicked,
  )
}

@Composable
private fun ChangeAddressEnterVillaInformationScreen(
  uiState: ChangeAddressUiState,
  navigateUp: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onAncillaryAreaChanged: (String) -> Unit,
  onNumberOfBathroomsChanged: (String) -> Unit,
  onIsSubletSelected: (Boolean) -> Unit,
  onSaveNewAddress: () -> Unit,
  onAddExtraBuildingClicked: () -> Unit,
  onRemoveExtraBuildingClicked: (ExtraBuilding) -> Unit,
) {
  if (uiState.errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = uiState.errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = stringResource(id = R.string.CHANGE_ADDRESS_INFORMATION_ABOUT_YOUR_HOUSE),
      style = MaterialTheme.typography.headlineMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
    Spacer(modifier = Modifier.weight(1f))
    InputTextField(
      value = uiState.yearOfConstruction.input,
      errorMessageRes = uiState.yearOfConstruction.errorMessageRes,
      onValueChange = onYearOfConstructionChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.ancillaryArea.input,
      errorMessageRes = uiState.ancillaryArea.errorMessageRes,
      onValueChange = onAncillaryAreaChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_ANCILLARY_AREA_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.numberOfBathrooms.input,
      errorMessageRes = uiState.numberOfBathrooms.errorMessageRes,
      onValueChange = onNumberOfBathroomsChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_BATHROOMS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    ChangeAddressSwitch(
      label = stringResource(id = R.string.CHANGE_ADDRESS_SUBLET_LABEL),
      checked = uiState.isSublet.input,
      onCheckedChange = onIsSubletSelected,
      onClick = { onIsSubletSelected(!uiState.isSublet.input) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    ExtraBuildingContainer(
      extraBuildings = uiState.extraBuildings,
      onAddExtraBuildingClicked = onAddExtraBuildingClicked,
      onRemoveExtraBuildingClicked = onRemoveExtraBuildingClicked,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    VectorInfoCard(
      text = stringResource(id = R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      onClick = onSaveNewAddress,
      modifier = Modifier.padding(horizontal = 16.dp),
      isLoading = uiState.isLoading,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeAddressEnterVillaInformationScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressEnterVillaInformationScreen(
        ChangeAddressUiState(),
        {},
        {},
        {},
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
