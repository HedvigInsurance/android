package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.AddBottomSheetState
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun AddCoInsuredBottomSheetContent(
  firstName: String?,
  lastName: String?,
  birthDate: LocalDate?,
  errorMessage: String?,
  isLoading: Boolean,
  enabled: Boolean,
  showManualInput: Boolean,
  onSsnChanged: (String) -> Unit,
  onSaveLabel: AddBottomSheetState.SaveButtonLabel,
  onContinue: () -> Unit,
  onManualInputSwitchChanged: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(stringResource(id = R.string.CONTRACT_ADD_COINSURED))
    Spacer(Modifier.height(24.dp))
    AnimatedVisibility(visible = showManualInput) {
      ManualInputFields(
        birthDate = birthDate,
        errorMessage = errorMessage,
        onBirthDateChanged = onBirthDateChanged,
        onFirstNameChanged = onFirstNameChanged,
        onLastNameChanged = onLastNameChanged,
      )
    }
    AnimatedVisibility(visible = !showManualInput) {
      FetchFromSsnFields(
        firstName = firstName,
        lastName = lastName,
        errorMessage = errorMessage,
        onSsnChanged = onSsnChanged,
        onContinue = onContinue,
      )
    }
    Spacer(Modifier.height(4.dp))
    HedvigCard(
      Modifier
        .fillMaxWidth()
        .clickable {
          onManualInputSwitchChanged(!showManualInput)
        },
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(id = R.string.CONTRACT_ADD_COINSURED_NO_SSN),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        endSlot = {
          Switch(
            checked = showManualInput,
            onCheckedChange = onManualInputSwitchChanged,
          )
        },
        spaceBetween = 4.dp,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = onSaveLabel.stringRes()),
      enabled = enabled,
      onClick = onContinue,
      isLoading = isLoading,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = onDismiss,
      text = stringResource(id = R.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
  }
}

@Composable
fun FetchFromSsnFields(
  firstName: String?,
  lastName: String?,
  errorMessage: String?,
  onSsnChanged: (String) -> Unit,
  onContinue: () -> Unit,
) {

  var ssnInput by remember { mutableStateOf("") }

  Column {
    HedvigTextField(
      value = ssnInput,
      label = {
        Text(stringResource(id = R.string.CONTRACT_PERSONAL_IDENTITY))
      },
      onValueChange = {
        onSsnChanged(it)
        ssnInput = it
      },
      errorText = errorMessage,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          onContinue()
        },
      ),
      modifier = Modifier.fillMaxWidth(),
    )
    AnimatedVisibility(
      visible = firstName?.isNotBlank() == true,
      modifier = Modifier.padding(top = 4.dp),
    ) {
      HedvigTextField(
        value = "$firstName $lastName",
        onValueChange = {},
        label = {
          Text(stringResource(id = R.string.FULL_NAME_TEXT))
        },
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
fun ManualInputFields(
  birthDate: LocalDate?,
  onBirthDateChanged: (LocalDate) -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  errorMessage: String?,
) {
  var firstNameInput by remember { mutableStateOf("") }
  var lastNameInput by remember { mutableStateOf("") }

  Column {
    DatePickerWithDialog(
      onSave = onBirthDateChanged,
      birthDate = birthDate,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(4.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigTextField(
          value = firstNameInput,
          label = {
            Text(stringResource(id = R.string.CONTRACT_FIRST_NAME))
          },
          onValueChange = {
            onFirstNameChanged(it)
            firstNameInput = it
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
      },
      endSlot = {
        HedvigTextField(
          value = lastNameInput,
          label = {
            Text(stringResource(id = R.string.CONTRACT_LAST_NAME))
          },
          onValueChange = {
            onLastNameChanged(it)
            lastNameInput = it
          },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
          ),
          modifier = Modifier.fillMaxWidth(),
        )
      },
      spaceBetween = 4.dp,
    )
    Spacer(Modifier.height(4.dp))
    AnimatedVisibility(visible = errorMessage != null) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Hedvig.WarningFilled,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.warningElement,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = errorMessage ?: "",
          color = MaterialTheme.colorScheme.warningElement,
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
}

@Composable
fun DatePickerWithDialog(
  modifier: Modifier = Modifier,
  birthDate: LocalDate?,
  onSave: (LocalDate) -> Unit,
) {
  val datePickerState = rememberDatePickerState()

  val selectedDateMillis: Long? = datePickerState.selectedDateMillis
  val locale = getLocale()
  val hedvigDateTimeFormatter = rememberHedvigBirthDateDateTimeFormatter()
  val selectedDate = remember(locale, selectedDateMillis, hedvigDateTimeFormatter) {
    if (selectedDateMillis == null) {
      null
    } else {
      Instant.fromEpochMilliseconds(selectedDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
    }
  }

  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            showDatePicker = false
            selectedDate?.let {
              onSave(selectedDate)
            }
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_save_button))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_cancel_button))
        }
      },
    ) {
      HedvigDatePicker(datePickerState = datePickerState)
    }
  }
  HedvigCard(modifier.clickable { showDatePicker = true }) {
    Text(
      text = if (birthDate != null) {
        hedvigDateTimeFormatter.format(birthDate.toJavaLocalDate())
      } else {
        stringResource(id = R.string.CONTRACT_BIRTH_DATE)
      },
      color = if (birthDate != null) {
        Color.Unspecified
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      },
      modifier = Modifier.padding(
        horizontal = 16.dp,
        vertical = 16.dp,
      ),
    )
  }
}

private fun AddBottomSheetState.SaveButtonLabel.stringRes() = when (this) {
  AddBottomSheetState.SaveButtonLabel.FETCH_INFO -> R.string.CONTRACT_SSN_FETCH_INFO
  AddBottomSheetState.SaveButtonLabel.ADD -> R.string.CONTRACT_ADD_COINSURED
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        firstName = null,
        lastName = "Testersson",
        birthDate = LocalDate.fromEpochDays(500),
        errorMessage = "Error",
        isLoading = false,
        showManualInput = true,
        enabled = true,
        onSsnChanged = {},
        onContinue = {},
        onManualInputSwitchChanged = {},
        onDismiss = {},
        onBirthDateChanged = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onSaveLabel = AddBottomSheetState.SaveButtonLabel.ADD,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentWithCoInsuredPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        firstName = "Test",
        lastName = "Testersson",
        birthDate = null,
        errorMessage = "Error",
        isLoading = false,
        enabled = true,
        showManualInput = false,
        onSsnChanged = {},
        onContinue = {},
        onManualInputSwitchChanged = {},
        onDismiss = {},
        onBirthDateChanged = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onSaveLabel = AddBottomSheetState.SaveButtonLabel.ADD,
      )
    }
  }
}
