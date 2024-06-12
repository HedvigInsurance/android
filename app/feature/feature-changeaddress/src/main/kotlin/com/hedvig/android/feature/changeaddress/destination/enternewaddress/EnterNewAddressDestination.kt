package com.hedvig.android.feature.changeaddress.destination.enternewaddress

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.ui.ChangeAddressSwitch
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import com.hedvig.android.feature.changeaddress.ui.MovingDateButton
import hedvig.resources.R
import java.util.Locale
import kotlinx.datetime.LocalDate

@Composable
internal fun EnterNewAddressDestination(
  viewModel: EnterNewAddressViewModel,
  onNavigateToVillaInformationDestination: (MovingParameters) -> Unit,
  navigateUp: () -> Unit,
  onNavigateToOfferDestination: (MovingParameters) -> Unit,
) {
  val uiState: EnterNewAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val paramsForOffers = uiState.navParamsForOfferDestination
  LaunchedEffect(paramsForOffers) {
    if (paramsForOffers != null) {
      viewModel.emit(EnterNewAddressEvent.ClearNavParams)
      onNavigateToOfferDestination(paramsForOffers)
    }
  }

  val paramsForVilla = uiState.navParamsForVillaDestination
  LaunchedEffect(paramsForVilla) {
    if (paramsForVilla != null) {
      viewModel.emit(EnterNewAddressEvent.ClearNavParams)
      onNavigateToVillaInformationDestination(paramsForVilla)
    }
  }

  ChangeAddressEnterNewAddressScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onErrorDialogDismissed = { viewModel.emit(EnterNewAddressEvent.DismissErrorDialog) },
    onStreetChanged = { street -> viewModel.emit(EnterNewAddressEvent.ChangeStreet(street)) },
    onPostalCodeChanged = { postalCode -> viewModel.emit(EnterNewAddressEvent.ChangePostalCode(postalCode)) },
    onSquareMetersChanged = { squareMeters -> viewModel.emit(EnterNewAddressEvent.ChangeSquareMeters(squareMeters)) },
    onCoInsuredChanged = { coInsured -> viewModel.emit(EnterNewAddressEvent.ChangeNumberInsured(coInsured)) },
    onMoveDateSelected = { date -> viewModel.emit(EnterNewAddressEvent.ChangeMoveDate(date)) },
    onIsStudentSelected = { isStudent -> viewModel.emit(EnterNewAddressEvent.ChangeIsStudent(isStudent)) },
    onSaveNewAddress = { viewModel.emit(EnterNewAddressEvent.ValidateInput) },
  )
}

@Composable
private fun ChangeAddressEnterNewAddressScreen(
  uiState: EnterNewAddressUiState,
  navigateUp: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onStreetChanged: (String) -> Unit,
  onPostalCodeChanged: (String) -> Unit,
  onSquareMetersChanged: (String) -> Unit,
  onCoInsuredChanged: (String) -> Unit,
  onMoveDateSelected: (LocalDate) -> Unit,
  onIsStudentSelected: (Boolean) -> Unit,
  onSaveNewAddress: () -> Unit,
) {
  if (uiState.errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = uiState.errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  if (uiState.isLoading) {
    CircularProgressIndicator()
  }

  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    var addressInput by remember {
      mutableStateOf(uiState.street.input ?: "")
    }
    var postalCodeInput by remember {
      mutableStateOf(uiState.postalCode.input ?: "")
    }
    var size by remember {
      mutableStateOf(uiState.squareMeters.input ?: "")
    }
    var insuredPeople by remember {
      mutableStateOf(uiState.numberInsured.input ?: "")
    }

    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = stringResource(id = R.string.CHANGE_ADDRESS_ENTER_NEW_ADDRESS_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
    Spacer(modifier = Modifier.weight(1f))
    InputTextField(
      value = addressInput,
      errorMessageRes = uiState.street.errorMessageRes,
      onValueChange = {
        addressInput = it
        onStreetChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = postalCodeInput,
      errorMessageRes = uiState.postalCode.errorMessageRes,
      onValueChange = {
        postalCodeInput = it
        onPostalCodeChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = size,
      errorMessageRes = uiState.squareMeters.errorMessageRes,
      onValueChange = {
        size = it
        onSquareMetersChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = insuredPeople,
      errorMessageRes = uiState.numberInsured.errorMessageRes,
      onValueChange = {
        insuredPeople = it
        onCoInsuredChanged(it)
      },
      label = stringResource(id = R.string.CHANGE_ADDRESS_CO_INSURED_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    val focusManager = LocalFocusManager.current
    uiState.datePickerUiState?.let {
      Spacer(modifier = Modifier.height(8.dp))
      MovingDateButton(
        onDateSelected = {
          onMoveDateSelected(it)
          focusManager.clearFocus()
        },
        // todo: Scaffold's modifier does not clear focus in this case,
        //  nor does MovingDateButton modifier, so it's annoying when keyboard opens again
        datePickerState = uiState.datePickerUiState.datePickerState,
        movingDate = uiState.movingDate,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    if (uiState.isEligibleForStudent) {
      Spacer(modifier = Modifier.height(8.dp))
      ChangeAddressSwitch(
        label = stringResource(id = R.string.CHANGE_ADDRESS_STUDENT_LABEL),
        checked = uiState.isStudent,
        onCheckedChange = onIsStudentSelected,
        onClick = { onIsStudentSelected(!uiState.isStudent) },
      )
    }
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
private fun PreviewChangeAddressEnterNewAddressScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressEnterNewAddressScreen(
        EnterNewAddressUiState(datePickerUiState = DatePickerUiState(Locale.ENGLISH, null)),
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
