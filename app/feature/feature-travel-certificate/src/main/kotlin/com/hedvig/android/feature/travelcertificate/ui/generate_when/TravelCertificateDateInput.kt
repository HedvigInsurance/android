package com.hedvig.android.feature.travelcertificate.ui.generate_when

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelCertificateDateInputDestination(
  viewModel: TravelCertificateDateInputViewModel,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (TravelCertificatePrimaryInput) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TravelCertificateDateInput(
    uiState = uiState,
    changeEmail = { viewModel.emit(TravelCertificateDateInputEvent.ChangeEmailInput(it)) },
    changeDate = { viewModel.emit(TravelCertificateDateInputEvent.ChangeDataInput(it)) },
    reload = { viewModel.emit(TravelCertificateDateInputEvent.RetryLoadData) },
    navigateUp = navigateUp,
    onNavigateToFellowTravellers = onNavigateToFellowTravellers,
    onNavigateToOverview = onNavigateToOverview,
    validateInput = { viewModel.emit(TravelCertificateDateInputEvent.ValidateInputAndChooseDirection) },
    nullifyInputValidity = { viewModel.emit(TravelCertificateDateInputEvent.NullifyInputValidity) },
  )
}

@Composable
private fun TravelCertificateDateInput(
  uiState: TravelCertificateDateInputUiState,
  changeEmail: (String) -> Unit,
  changeDate: (LocalDate) -> Unit,
  reload: () -> Unit,
  navigateUp: () -> Unit,
  onNavigateToFellowTravellers: (TravelCertificatePrimaryInput) -> Unit,
  onNavigateToOverview: (TravelCertificateUrl) -> Unit,
  validateInput: () -> Unit,
  nullifyInputValidity: () -> Unit,
) {
  var toast by remember { mutableStateOf<Int?>(null) }
  val context = LocalContext.current
  val errorMsg = stringResource(id = R.string.travel_certificate_email_empty_error)

  LaunchedEffect(toast) {
    val stringRes = toast
    if (stringRes != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
  } // todo: do we do anything here, do we show toast or smth? is it good idea to show th same msg only once?

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
      onNavigateToOverview(uiState.travelCertificateUrl)
    }

    is TravelCertificateDateInputUiState.Success -> {
      LaunchedEffect(uiState.inputValid) {
        if (uiState.inputValid == true) {
          if (uiState.primaryInput != null) {
            onNavigateToFellowTravellers(uiState.primaryInput)
            nullifyInputValidity()
          }
        } else if (uiState.inputValid == false) {
          toast = R.string.travel_certificate_email_empty_error
        }
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
          onDateSelected = changeDate,
          datePickerState = uiState.datePickerState,
          dateValidator = uiState.dateValidator,
          travelDate = uiState.travelDate,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        EmailTextField(
          email = uiState.email,
          onStreetChanged = changeEmail,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          onClick = {
            validateInput()
          },
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
private fun EmailTextField(email: String?, onStreetChanged: (String) -> Unit, modifier: Modifier = Modifier) {
  HedvigTextField(
    value = TextFieldValue(
      // todo: had a problem with a jumping cursor, this works, but not sure if it is a best solution
      text = email ?: "",
      selection = TextRange((email ?: "").length),
    ),
    withNewDesign = true,
    onValueChange = { onStreetChanged(it.text) },
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
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
          hasCoEnsured = false,
          datePickerState = DatePickerState(null, null, 2020..2024, DisplayMode.Picker),
          dateValidator = { true },
          daysValid = 40,
          inputValid = null,
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
