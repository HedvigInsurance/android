package com.hedvig.android.feature.changeaddress.destination

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
import com.hedvig.android.feature.changeaddress.DatePickerUiState
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.ui.ChangeAddressSwitch
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import com.hedvig.android.feature.changeaddress.ui.MovingDateButton
import hedvig.resources.R
import java.util.Locale
import kotlinx.datetime.LocalDate

@Composable
internal fun ChangeAddressEnterNewAddressDestination(
  viewModel: ChangeAddressViewModel,
  onNavigateToVillaInformationDestination: () -> Unit,
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

  ChangeAddressEnterNewAddressScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onStreetChanged = viewModel::onStreetChanged,
    onPostalCodeChanged = viewModel::onPostalCodeChanged,
    onSquareMetersChanged = viewModel::onSquareMetersChanged,
    onCoInsuredChanged = viewModel::onCoInsuredChanged,
    onMoveDateSelected = viewModel::onMoveDateSelected,
    onIsStudentSelected = viewModel::onIsStudentChanged,
    onSaveNewAddress = {
      val isInputValid = viewModel.validateAddressInput()
      if (isInputValid) {
        if (uiState.housingType.input == HousingType.VILLA) {
          onNavigateToVillaInformationDestination()
        } else {
          viewModel.onSubmitNewAddress()
        }
      }
    },
  )
}

@Composable
private fun ChangeAddressEnterNewAddressScreen(
  uiState: ChangeAddressUiState,
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
      value = uiState.street.input,
      errorMessageRes = uiState.street.errorMessageRes,
      onValueChange = onStreetChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.postalCode.input,
      errorMessageRes = uiState.postalCode.errorMessageRes,
      onValueChange = onPostalCodeChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.squareMeters.input,
      errorMessageRes = uiState.squareMeters.errorMessageRes,
      onValueChange = onSquareMetersChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.numberInsured.input,
      errorMessageRes = uiState.numberInsured.errorMessageRes,
      onValueChange = onCoInsuredChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_CO_INSURED_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    uiState.datePickerUiState?.let {
      Spacer(modifier = Modifier.height(8.dp))
      MovingDateButton(
        onDateSelected = { onMoveDateSelected(it) },
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
        ChangeAddressUiState(datePickerUiState = DatePickerUiState(Locale.ENGLISH, null)),
        {}, {}, {}, {}, {}, {}, {}, {}, {},
      )
    }
  }
}
