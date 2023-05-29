package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.CoInsured
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun GenerateTravelCertificateInput(
  uiState: TravelCertificateInputState,
  navigateBack: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onEmailChanged: (String) -> Unit,
  onCoInsuredClicked: (CoInsured) -> Unit,
  onAddCoInsuredClicked: () -> Unit,
  onIncludeMemberClicked: (Boolean) -> Unit,
  onTravelDateSelected: (LocalDate) -> Unit,
  onContinue: () -> Unit,
  onSuccess: (TravelCertificateUri) -> Unit,
) {
  if (uiState.errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = uiState.errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  LaunchedEffect(uiState.travelCertificateUrl) {
    uiState.travelCertificateUrl?.let {
      onSuccess(it)
    }
  }

  if (uiState.isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = navigateBack,
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Spacer(modifier = Modifier.height(48.dp))
      Text(
        text = "Travel information",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(64.dp))
      EmailTextField(
        email = uiState.email,
        onStreetChanged = onEmailChanged,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(8.dp))
      MovingDateButton(
        onDateSelected = { onTravelDateSelected(it) },
        uiState = uiState,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(id = R.string.travel_certificate_included_members_title),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(8.dp))
      LargeContainedButton(
        onClick = { onIncludeMemberClicked(!uiState.includeMember) },
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.squircle,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(stringResource(id = R.string.travel_certificate_me))
          if (uiState.includeMember) {
            Icon(
              painter = painterResource(id = com.hedvig.android.core.designsystem.R.drawable.ic_checkmark),
              tint = MaterialTheme.colorScheme.onSurface,
              contentDescription = "include me",
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))

      uiState.coInsured.errorMessageRes?.let {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          // Emulate the same design that the supporting text of the TextField has
          modifier = Modifier.padding(
            start = 16.dp,
            top = 4.dp,
            end = 16.dp,
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
            text = "Ange vilka som täcks av certifikatet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }

      uiState.coInsured.input.map { coInsured ->
        Spacer(modifier = Modifier.height(8.dp))

        LargeContainedButton(
          onClick = { onCoInsuredClicked(coInsured) },
          modifier = Modifier.padding(horizontal = 16.dp),
          shape = MaterialTheme.shapes.squircle,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
          ),
        ) {
          Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(coInsured.name)
          }
        }
      }
      TextButton(
        onClick = {
          onAddCoInsuredClicked()
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Text(stringResource(id = R.string.travel_certificate_add_member))
      }

      Spacer(modifier = Modifier.weight(1f))
      LargeContainedButton(
        onClick = onContinue,
        shape = MaterialTheme.shapes.squircle,
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Text(stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL))
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun EmailTextField(
  email: ValidatedInput<String?>,
  onStreetChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = email.input ?: "",
    onValueChange = { onStreetChanged(it) },
    errorText = email.errorMessageRes?.let { stringResource(it) },
    label = {
      Text("Email")
    },
    maxLines = 1,
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
private fun MovingDateButton(
  onDateSelected: (LocalDate) -> Unit,
  uiState: TravelCertificateInputState,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker && uiState.datePickerState != null) {
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
        dateValidator = { uiState.dateValidator(it) },
      )
    }
  }

  Column(modifier) {
    val errorTextResId = if (uiState.travelDate.errorMessageRes != null) {
      uiState.travelDate.errorMessageRes
    } else {
      null
    }
    val dateHasError = errorTextResId != null
    HedvigCard(
      onClick = { showDatePicker = true },
      shape = MaterialTheme.shapes.squircle,
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
            text = "Travel date",
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = uiState.travelDate.input?.toString()
              ?: stringResource(R.string.CHANGE_ADDRESS_SELECT_MOVING_DATE_LABEL),
            style = MaterialTheme.typography.headlineSmall,
          )
        }
        Spacer(Modifier.width(16.dp))
        Icon(
          painter = painterResource(
            id = com.hedvig.android.core.designsystem.R.drawable.ic_drop_down_indicator,
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
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
fun GenerateTravelCertificateInputPreview() {
  HedvigTheme {
    Surface {
      GenerateTravelCertificateInput(
        uiState = mockUiState,
        navigateBack = {},
        onErrorDialogDismissed = {},
        onEmailChanged = {},
        onCoInsuredClicked = {},
        onAddCoInsuredClicked = {},
        onIncludeMemberClicked = {},
        onTravelDateSelected = {},
        onContinue = {},
        onSuccess = {},
      )
    }
  }
}

val mockUiState = TravelCertificateInputState(
  email = ValidatedInput(input = null),
  travelDate = ValidatedInput(input = null),
  coInsured = ValidatedInput(
    input = listOf(
      CoInsured(
        id = "123",
        name = "Hugo",
        ssn = "199101131093",
      ),
      CoInsured(
        id = "123",
        name = "Stelios",
        ssn = "199101131093",
      ),
    ),
  ),
  includeMember = true,
  isLoading = false,
)
