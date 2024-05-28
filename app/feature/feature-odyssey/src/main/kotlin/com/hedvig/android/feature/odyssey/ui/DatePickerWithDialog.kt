package com.hedvig.android.feature.odyssey.ui

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun DatePickerWithDialog(
  uiState: DatePickerUiState,
  canInteract: Boolean,
  startText: String,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = { showDatePicker = false },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.general_save_button))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            uiState.clearDateSelection()
            showDatePicker = false
          },
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(stringResource(R.string.GENERAL_NOT_SURE))
        }
      },
    ) {
      HedvigDatePicker(datePickerState = uiState.datePickerState)
    }
  }

  val selectedDateMillis: Long? = uiState.datePickerState.selectedDateMillis
  val locale = getLocale()
  val hedvigDateTimeFormatter = rememberHedvigDateTimeFormatter()
  val selectedDateText: String? = remember(locale, selectedDateMillis, hedvigDateTimeFormatter) {
    if (selectedDateMillis == null) {
      null
    } else {
      Instant.fromEpochMilliseconds(selectedDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toJavaLocalDate()
        .format(hedvigDateTimeFormatter)
    }
  }
  HedvigBigCard(
    onClick = { showDatePicker = true },
    hintText = startText,
    inputText = selectedDateText,
    enabled = canInteract,
    modifier = modifier,
  )
}

@Stable
internal class DatePickerUiState(
  locale: Locale,
  initiallySelectedDate: LocalDate?,
  minDate: LocalDate = LocalDate(1900, 1, 1),
  maxDate: LocalDate = LocalDate(2100, 1, 1),
) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    locale = locale,
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = DisplayMode.Picker,
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minDateInMillis..maxDateInMillis

      override fun isSelectableYear(year: Int): Boolean = year in yearRange
    },
  )

  fun clearDateSelection() {
    datePickerState.selectedDateMillis = null
  }
}

@HedvigPreview
@Composable
private fun PreviewDatePickerWithDialog(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasSelectedDate: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DatePickerWithDialog(
        uiState = DatePickerUiState(
          locale = Locale.ENGLISH,
          initiallySelectedDate = if (hasSelectedDate) LocalDate(2100, 1, 1) else null,
        ),
        canInteract = true,
        startText = "Date of incident",
      )
    }
  }
}
