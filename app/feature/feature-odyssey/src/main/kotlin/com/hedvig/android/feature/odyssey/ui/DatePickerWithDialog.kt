package com.hedvig.android.feature.odyssey.ui

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
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import java.util.Locale
import kotlin.time.Instant
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
    HedvigDatePicker(
      datePickerState = uiState.datePickerState,
      onDismissRequest = { showDatePicker = false },
      onConfirmRequest = { showDatePicker = false },
      dismissButton = {
        HedvigTextButton(
          text = stringResource(R.string.GENERAL_NOT_SURE),
          onClick = {
            uiState.clearDateSelection()
            showDatePicker = false
          },
          buttonSize = Medium,
        )
      },
    )
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
    labelText = startText,
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

  val datePickerState = HedvigDatePickerState(
    locale = locale,
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = HedvigDisplayMode.Picker,
    selectableDates = object : HedvigSelectableDates {
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
