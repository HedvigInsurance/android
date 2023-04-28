package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hedvig.android.core.designsystem.component.button.FormRowCard
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.getLocale
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun DatePickerRowCard(
  uiState: DatePickerUiState,
  canInteract: Boolean,
  startText: String,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    Dialog(onDismissRequest = { showDatePicker = false }) {
      Surface(
        Modifier.clip(MaterialTheme.shapes.extraLarge),
      ) {
        Column {
          HedvigDatePicker(
            datePickerState = uiState.datePickerState,
            dateValidator = uiState::validateDate,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            TextButton(
              onClick = {
                uiState.clearDateSelection()
                showDatePicker = false
              },
              shape = MaterialTheme.shapes.medium,
            ) {
              Text(stringResource(R.string.GENERAL_NOT_SURE))
            }
            TextButton(
              onClick = { showDatePicker = false },
              shape = MaterialTheme.shapes.medium,
            ) {
              Text(stringResource(R.string.ALERT_OK))
            }
          }
        }
      }
    }
  }

  FormRowCard(
    modifier = modifier,
    enabled = canInteract,
    onClick = { showDatePicker = true },
  ) {
    Text(startText)
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    HedvigCard(
      colors = CardDefaults.outlinedCardColors(MaterialTheme.colorScheme.surfaceVariant),
    ) {
      val selectedDateMillis = uiState.datePickerState.selectedDateMillis
      val locale = getLocale()
      val dateText = remember(selectedDateMillis, locale) {
        if (selectedDateMillis == null) {
          "2020 Feb 20" // Placeholder text to take the right amount of space
        } else {
          Instant.fromEpochMilliseconds(selectedDateMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .toJavaLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))
        }
      }
      Text(
        text = dateText,
        modifier = Modifier
          .padding(12.dp)
          .alpha(if (selectedDateMillis != null) 1f else 0f),
      )
    }
  }
}

@Stable
internal class DatePickerUiState(
  initiallySelectedDate: LocalDate?,
  minDate: LocalDate = LocalDate(1900, 1, 1),
  maxDate: LocalDate = LocalDate(2100, 1, 1),
) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

  val datePickerState = DatePickerState(
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearsRange = minDate.year..maxDate.year,
  )

  fun clearDateSelection() {
    @Suppress("INVISIBLE_MEMBER") // Resetting the date exists in material3 1.1.0-alpha08, for now access internal code
    datePickerState.selectedDate = null
  }

  fun validateDate(selectedDateEpochMillis: Long): Boolean {
    return selectedDateEpochMillis in minDateInMillis..maxDateInMillis
  }
}

@HedvigPreview
@Composable
private fun PreviewDatePickerRowCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DatePickerRowCard(
        DatePickerUiState(null),
        true,
        "Date of incident",
      )
    }
  }
}
