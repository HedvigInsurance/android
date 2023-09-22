package com.hedvig.android.feature.changeaddress.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.ui.ValidatedInput
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun MovingDateButton(
  onDateSelected: (LocalDate) -> Unit,
  datePickerState: DatePickerState,
  movingDate: ValidatedInput<LocalDate?>,
  validate: (Long) -> Boolean,
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
        dateValidator = validate,
      )
    }
  }

  Column(modifier) {
    val errorTextResId = if (movingDate.errorMessageRes != null) {
      movingDate.errorMessageRes
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
            text = stringResource(id = R.string.CHANGE_ADDRESS_MOVING_DATE_LABEL),
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = movingDate.input?.toString()
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
