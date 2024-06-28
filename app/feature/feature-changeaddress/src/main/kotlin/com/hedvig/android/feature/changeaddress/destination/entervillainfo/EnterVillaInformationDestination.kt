package com.hedvig.android.feature.changeaddress.destination.entervillainfo

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.ui.ChangeAddressSwitch
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import com.hedvig.android.feature.changeaddress.ui.extrabuildings.ExtraBuildingBottomSheet
import com.hedvig.android.feature.changeaddress.ui.extrabuildings.ExtraBuildingContainer
import hedvig.resources.R

@Composable
internal fun EnterVillaInformationDestination(
  viewModel: EnterVillaInformationViewModel,
  navigateUp: () -> Unit,
  onNavigateToOfferDestination: (MovingParameters) -> Unit,
) {
  val uiState: EnterVillaInformationUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val focusManager = LocalFocusManager.current
  val movingParameters = uiState.movingParameters
  LaunchedEffect(movingParameters) {
    if (movingParameters != null) {
      viewModel.emit(EnterVillaInformationEvent.ClearNavParameters)
      onNavigateToOfferDestination(movingParameters)
    }
  }

  var showExtraBuildingsBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showExtraBuildingsBottomSheet) {
    ExtraBuildingBottomSheet(
      extraBuildingTypes = uiState.extraBuildingTypes,
      onDismiss = {
        showExtraBuildingsBottomSheet = false
      },
      onSave = {
        showExtraBuildingsBottomSheet = false
        viewModel.emit(EnterVillaInformationEvent.AddExtraBuilding(it))
      },
      onVisibleChange = { showExtraBuildingsBottomSheet = it },
    )
  }

  ChangeAddressEnterVillaInformationScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onYearOfConstructionChanged = {
      viewModel.emit(EnterVillaInformationEvent.ChangeYearOfConstruction(it))
    },
    onAncillaryAreaChanged = {
      viewModel.emit(EnterVillaInformationEvent.ChangeAncillaryArea(it))
    },
    onNumberOfBathroomsChanged = {
      viewModel.emit(EnterVillaInformationEvent.ChangeNumberOfBathrooms(it))
    },
    onIsSubletSelected = {
      viewModel.emit(EnterVillaInformationEvent.ChangeIsSublet(it))
    },
    onSaveNewAddress = {
      viewModel.emit(EnterVillaInformationEvent.SubmitNewAddress)
    },
    onAddExtraBuildingClicked = {
      showExtraBuildingsBottomSheet = true
      focusManager.clearFocus()
    },
    onRemoveExtraBuildingClicked = { viewModel.emit(EnterVillaInformationEvent.RemoveExtraBuildingClicked(it)) },
  )
}

@Composable
private fun ChangeAddressEnterVillaInformationScreen(
  uiState: EnterVillaInformationUiState,
  navigateUp: () -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onAncillaryAreaChanged: (String) -> Unit,
  onNumberOfBathroomsChanged: (String) -> Unit,
  onIsSubletSelected: (Boolean) -> Unit,
  onSaveNewAddress: () -> Unit,
  onAddExtraBuildingClicked: () -> Unit,
  onRemoveExtraBuildingClicked: (ExtraBuilding) -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    var year by remember {
      mutableStateOf(uiState.yearOfConstruction.input ?: "")
    }
    var ancillary by remember {
      mutableStateOf(uiState.ancillaryArea.input ?: "")
    }
    var bathrooms by remember {
      mutableStateOf(uiState.numberOfBathrooms.input ?: "")
    }
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
      value = year,
      errorMessageRes = uiState.yearOfConstruction.errorMessageRes,
      onValueChange = {
        year = it
        onYearOfConstructionChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = ancillary,
      errorMessageRes = uiState.ancillaryArea.errorMessageRes,
      onValueChange = {
        ancillary = it
        onAncillaryAreaChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_ANCILLARY_AREA_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = bathrooms,
      errorMessageRes = uiState.numberOfBathrooms.errorMessageRes,
      onValueChange = {
        bathrooms = it
        onNumberOfBathroomsChanged(it)
      },
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
        EnterVillaInformationUiState(
          extraBuildingTypes = listOf(),
        ),
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
