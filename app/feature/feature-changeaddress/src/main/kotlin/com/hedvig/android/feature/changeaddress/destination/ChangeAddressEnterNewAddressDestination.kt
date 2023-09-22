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
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import com.hedvig.android.feature.changeaddress.ui.MovingDateButton
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun ChangeAddressEnterNewDestination(
  viewModel: ChangeAddressViewModel,
  onContinue: () -> Unit,
  close: () -> Unit,
  onQuotesReceived: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val quotes = uiState.quotes
  LaunchedEffect(quotes) {
    if (quotes.isNotEmpty()) {
      onQuotesReceived()
    }
  }

  ChangeAddressEnterNewScreen(
    uiState = uiState,
    close = close,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onStreetChanged = viewModel::onStreetChanged,
    onPostalCodeChanged = viewModel::onPostalCodeChanged,
    onSquareMetersChanged = viewModel::onSquareMetersChanged,
    onCoInsuredChanged = viewModel::onCoInsuredChanged,
    onMoveDateSelected = viewModel::onMoveDateSelected,
    onSaveNewAddress = {
      if (uiState.housingType.input == HousingType.VILLA) {
        onContinue()
      } else {
        viewModel.onSubmitNewAddress()
      }
    },
  )
}

@Composable
private fun ChangeAddressEnterNewScreen(
  uiState: ChangeAddressUiState,
  close: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onStreetChanged: (String) -> Unit,
  onPostalCodeChanged: (String) -> Unit,
  onSquareMetersChanged: (String) -> Unit,
  onCoInsuredChanged: (String) -> Unit,
  onMoveDateSelected: (LocalDate) -> Unit,
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
    navigateUp = close,
    modifier = Modifier.clearFocusOnTap(),
    topAppBarActionType = TopAppBarActionType.CLOSE,
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
      value = uiState.street,
      onValueChange = onStreetChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.postalCode,
      onValueChange = onPostalCodeChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.squareMeters,
      onValueChange = onSquareMetersChanged,
      label = stringResource(id = R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
      ),
    )
    Spacer(modifier = Modifier.height(8.dp))
    InputTextField(
      value = uiState.numberCoInsured,
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
        validate = {
          uiState.datePickerUiState.validateDate(it)
        },
        modifier = Modifier.padding(horizontal = 16.dp),
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
private fun PreviewChangeAddressEnterNewScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressEnterNewScreen(
        ChangeAddressUiState(),
        {}, {}, {}, {}, {}, {}, {}, {},
      )
    }
  }
}
