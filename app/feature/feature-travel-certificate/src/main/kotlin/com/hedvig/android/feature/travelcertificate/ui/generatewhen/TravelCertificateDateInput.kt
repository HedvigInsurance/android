package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.onInfoElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronDown
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelCertificateDateInputDestination(
  viewModel: TravelCertificateDateInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (
    TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  ) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateDateInput(
    uiState = uiState,
    onDateChanged = { viewModel.emit(TravelCertificateDateInputEvent.ChangeDateInput(it)) },
    reload = { viewModel.emit(TravelCertificateDateInputEvent.RetryLoadData) },
    navigateUp = navigateUp,
    onNavigateToFellowTravellers = onNavigateToFellowTravellers,
    onNavigateToOverview = onNavigateToOverview,
    submitInput = { viewModel.emit(TravelCertificateDateInputEvent.Submit) },
    nullifyPrimaryInput = { viewModel.emit(TravelCertificateDateInputEvent.NullifyPrimaryInput) },
    onEmailChanged = { viewModel.emit(TravelCertificateDateInputEvent.ChangeEmailInput(it))  }
  )
}

@Composable
private fun TravelCertificateDateInput(
  uiState: TravelCertificateDateInputUiState,
  onDateChanged: (LocalDate) -> Unit,
  onEmailChanged: (String) -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (
    TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  ) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  submitInput: () -> Unit,
  nullifyPrimaryInput: () -> Unit,
) {
  when (uiState) {
    TravelCertificateDateInputUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(retry = reload, modifier = Modifier.weight(1f))
      }
    }

    TravelCertificateDateInputUiState.Loading -> HedvigFullScreenCenterAlignedProgress()

    is TravelCertificateDateInputUiState.UrlFetched -> {
      LaunchedEffect(Unit) {
        onNavigateToOverview(uiState.travelCertificateUrl)
      }
    }

    is TravelCertificateDateInputUiState.Success -> {
      var errorMessageRes by remember { mutableStateOf<Int?>(null) }

      LaunchedEffect(uiState.errorMessageRes, uiState.primaryInput) {
        if (uiState.errorMessageRes == null) {
          if (uiState.primaryInput != null) {
            onNavigateToFellowTravellers(uiState.primaryInput)
            nullifyPrimaryInput()
          }
        } else {
          errorMessageRes = uiState.errorMessageRes
        }
      }

      var emailInput by remember {
        mutableStateOf(uiState.email ?: "")
      }

      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        Spacer(Modifier.height(24.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_when_is_your_trip),
          style = MaterialTheme.typography.headlineMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        MovingDateButton(
          onDateSelected = onDateChanged,
          datePickerState = uiState.datePickerState,
          dateValidator = uiState.dateValidator,
          travelDate = uiState.travelDate,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        EmailTextField(
          email = emailInput,
          onEmailChanged = {
            emailInput = it
            errorMessageRes = null
            onEmailChanged(it)
          },
          modifier = Modifier.padding(horizontal = 16.dp),
          errorText = errorMessageRes?.let { stringResource(id = it) },
        )
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          onClick = submitInput,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          Text(
            text = stringResource(id = R.string.general_continue_button),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun EmailTextField(
  email: String,
  errorText: String?,
  onEmailChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
//  var errorMessage by remember { mutableStateOf(errorText)}
  HedvigTextField(
    value = email,
    errorText = errorText,
    withNewDesign = true,
    onValueChange = {
      onEmailChanged(it)
    },
    label = {
      Text("Email")
    },
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun MovingDateButton(
  onDateSelected: (LocalDate) -> Unit,
  datePickerState: DatePickerState,
  dateValidator: (Long) -> Boolean,
  travelDate: LocalDate,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              val selectedDate = Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
              datePickerState.setSelection(it)
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
        datePickerState = datePickerState,
        dateValidator = { dateValidator(it) },
      )
    }
  }

  Column(modifier) {
    HedvigCard(
      onClick = { showDatePicker = true },
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(Modifier.weight(1f)) {
          Text(
            text = stringResource(id = R.string.travel_certificate_start_date_title),
            style = MaterialTheme.typography.bodySmall,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = travelDate.toString(),
            style = MaterialTheme.typography.headlineSmall,
          )
        }
        Spacer(Modifier.width(16.dp))
        Icon(
          imageVector = Icons.Hedvig.ChevronDown,
          tint = MaterialTheme.colorScheme.onInfoElement,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateDateInput() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TravelCertificateDateInput(
        TravelCertificateDateInputUiState.Success(
          "id",
          "emaild",
          hasCoInsured = false,
          datePickerState = DatePickerState(null, null, 2020..2024, DisplayMode.Picker),
          dateValidator = { true },
          travelDate = LocalDate(2023,1,1),
          daysValid = 40,
          errorMessageRes = null,
          primaryInput = null,
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
