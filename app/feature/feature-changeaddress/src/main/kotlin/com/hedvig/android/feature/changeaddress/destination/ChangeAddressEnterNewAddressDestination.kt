package com.hedvig.android.feature.changeaddress.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.ui.AddressInfoCard
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ChangeAddressEnterNewDestination(
  viewModel: ChangeAddressViewModel,
  navigateBack: () -> Unit,
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
    navigateBack = navigateBack,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onStreetChanged = viewModel::onStreetChanged,
    onPostalCodeChanged = viewModel::onPostalCodeChanged,
    onSquareMetersChanged = viewModel::onSquareMetersChanged,
    onCoInsuredChanged = viewModel::onCoInsuredChanged,
    onMoveDateSelected = viewModel::onMoveDateSelected,
    onSaveNewAddress = viewModel::onSaveNewAddress,
  )
}

@Composable
private fun ChangeAddressEnterNewScreen(
  uiState: ChangeAddressUiState,
  navigateBack: () -> Unit,
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
    navigateUp = navigateBack,
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
    Spacer(modifier = Modifier.weight(1f))
    AddressTextField(
      street = uiState.street,
      onStreetChanged = onStreetChanged,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    PostalCodeTextField(
      postalCode = uiState.postalCode,
      onPostalCodeChanged = onPostalCodeChanged,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    LivingSpaceTextField(
      squareMeters = uiState.squareMeters,
      onSquareMetersChanged = onSquareMetersChanged,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    NumberOfCoInsuredTextField(
      numberCoInsured = uiState.numberCoInsured,
      onCoInsuredChanged = onCoInsuredChanged,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    MovingDateButton(
      onDateSelected = { onMoveDateSelected(it) },
      uiState = uiState,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    AddressInfoCard(
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

@Composable
private fun AddressTextField(
  street: ValidatedInput<String?>,
  onStreetChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = street.input ?: "",
    onValueChange = { onStreetChanged(it) },
    errorText = street.errorMessageRes?.let { stringResource(it) },
    label = {
      Text(stringResource(R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL))
    },
    withNewDesign = true,
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun PostalCodeTextField(
  postalCode: ValidatedInput<String?>,
  onPostalCodeChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = postalCode.input ?: "",
    onValueChange = { onPostalCodeChanged(it) },
    errorText = postalCode.errorMessageRes?.let { stringResource(it) },
    label = {
      Text(stringResource(R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL))
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
    ),
    withNewDesign = true,
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun LivingSpaceTextField(
  squareMeters: ValidatedInput<String?>,
  onSquareMetersChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = squareMeters.input ?: "",
    onValueChange = { onSquareMetersChanged(it) },
    errorText = squareMeters.errorMessageRes?.let { stringResource(it) },
    label = {
      Text(stringResource(R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL))
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
    ),
    withNewDesign = true,
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun NumberOfCoInsuredTextField(
  numberCoInsured: ValidatedInput<String?>,
  onCoInsuredChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = if (numberCoInsured.input != null) {
      numberCoInsured.input.toString()
    } else {
      "1"
    },
    onValueChange = { onCoInsuredChanged(it) },
    errorText = numberCoInsured.errorMessageRes?.let { stringResource(it) },
    label = {
      Text(stringResource(R.string.CHANGE_ADDRESS_CO_INSURED_LABEL))
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
    ),
    withNewDesign = true,
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun MovingDateButton(
  onDateSelected: (LocalDate) -> Unit,
  uiState: ChangeAddressUiState,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            uiState.datePickerState.selectedDateMillis?.let {
              val selectedDate = Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
              uiState.datePickerState.setSelection(it)
              onDateSelected(selectedDate)
            }

            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.ALERT_OK))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_close_button))
        }
      },
    ) {
      HedvigDatePicker(
        datePickerState = uiState.datePickerState,
        dateValidator = { true }, // TODO Only allow future dates?
      )
    }
  }

  Column(modifier) {
    val errorTextResId = if (uiState.movingDate.errorMessageRes != null) {
      uiState.movingDate.errorMessageRes
    } else {
      null
    }
    val dateHasError = errorTextResId != null
    HedvigCard(
      onClick = { showDatePicker = true },
      colors = CardDefaults.outlinedCardColors(
        containerColor = if (dateHasError) {
          MaterialTheme.colorScheme.warningContainer
        } else {
          MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (dateHasError) {
          MaterialTheme.colorScheme.onWarningContainer
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
      ),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(Modifier.weight(1f)) {
          Text(
            text = stringResource(
              id = R.string.CHANGE_ADDRESS_MOVING_DATE_LABEL,
            ),
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = uiState.movingDate.input?.toString()
              ?: stringResource(R.string.CHANGE_ADDRESS_SELECT_MOVING_DATE_LABEL),
            style = MaterialTheme.typography.headlineSmall,
          )
        }
        Spacer(Modifier.width(16.dp))
        Icon(
          painter = painterResource(
            id = com.hedvig.android.core.design.system.R.drawable.ic_drop_down_indicator,
          ),
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      }
    }
    if (errorTextResId != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        // Emulate the same design that the supporting text of the TextField has
        modifier = Modifier.padding(
          start = 4.dp,
          top = 4.dp,
          end = 4.dp,
        ),
      ) {
        Icon(
          imageVector = Icons.Rounded.Warning,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.warningElement,
        )
        Spacer(Modifier.width(6.dp))
        Text(
          text = stringResource(errorTextResId),
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }
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
