package com.hedvig.android.design.system.internals

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import hedvig.resources.R

@Composable
fun HedvigDatePickerInternal(
  selectedDateMillis: Long?,
  displayedMonthMillis: Long?,
  yearRange: IntRange,
  minDateInMillis: Long,
  maxDateInMillis: Long,
  onDismissRequest: () -> Unit,
  onConfirmRequest: () -> Unit,
  onSelectedDateChanged: (Long?) -> Unit,
  hedvigDatePickerColors: HedvigDatePickerColors,
  modifier: Modifier = Modifier,
) {
  val hedvigColors = with(hedvigDatePickerColors) {
    DatePickerDefaults.colors(
      selectedDayContainerColor = selectedDayContainerColor,
      selectedDayContentColor = selectedDayContentColor,
      todayContentColor = todayContentColor,
      todayDateBorderColor = Color.Transparent,
      containerColor = containerColor,
      titleContentColor = titleContentColor,
      headlineContentColor = headlineContentColor,
      weekdayContentColor = weekdayContentColor,
      subheadContentColor = subheadContentColor,
      navigationContentColor = navigationContentColor,
      yearContentColor = yearContentColor,
      disabledYearContentColor = disabledYearContentColor,
      currentYearContentColor = currentYearContentColor,
      selectedYearContentColor = selectedYearContentColor,
      disabledSelectedYearContentColor = disabledSelectedYearContentColor,
      selectedYearContainerColor = selectedYearContainerColor,
      dayContentColor = dayContentColor,
      disabledDayContentColor = disabledDayContentColor,
      dayInSelectionRangeContentColor = dayInSelectionRangeContentColor,
      dividerColor = dividerColor,
      dateTextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = dateTextColor,
        focusedContainerColor = dateTextContainerColor,
        unfocusedContainerColor = dateTextContainerColor,
        focusedIndicatorColor = dateTextColor,
        cursorColor = dateTextColor,
        focusedLabelColor = dateTextColor,
        unfocusedLabelColor = dateTextColor,
      ),
    )
  }
  val state = rememberDatePickerState(
    initialSelectedDateMillis = selectedDateMillis,
    initialDisplayedMonthMillis = displayedMonthMillis,
    yearRange = yearRange,
    selectableDates = SelectableDatesImpl(
      minDateInMillis = minDateInMillis,
      maxDateInMillis = maxDateInMillis,
      yearRange = yearRange,
    ),
  )
  LaunchedEffect(state.selectedDateMillis) {
    onSelectedDateChanged(state.selectedDateMillis)
  }
  DatePickerDialog(
    onDismissRequest = onDismissRequest,
    colors = hedvigColors,
    confirmButton = {
      TextButton(
        shape = MaterialTheme.shapes.medium,
        onClick = onConfirmRequest,
        colors = ButtonDefaults.buttonColors(
          contentColor = hedvigDatePickerColors.textButtonColor,
          containerColor = Color.Transparent,
        ),
      ) {
        Text(text = stringResource(R.string.general_save_button))
      }
    },
  ) {
    HedvigDatePicker(
      modifier = modifier,
      datePickerState = state,
      colors = hedvigColors,
    )
  }
}

@Composable
fun HedvigDatePicker(
  datePickerState: DatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
  colors: DatePickerColors = DatePickerDefaults.colors(),
) {
  DatePicker(
    state = datePickerState,
    dateFormatter = dateFormatter,
    colors = colors,
    modifier = modifier,
  )
}

@Stable
data class SelectableDatesImpl(
  val minDateInMillis: Long,
  val maxDateInMillis: Long,
  val yearRange: IntRange,
) : SelectableDates {
  override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minDateInMillis..maxDateInMillis

  override fun isSelectableYear(year: Int): Boolean = year in yearRange
}

data class HedvigDatePickerColors(
  val selectedDayContainerColor: Color,
  val selectedDayContentColor: Color,
  val todayContentColor: Color,
  val todayDateBorderColor: Color,
  val containerColor: Color,
  val textButtonColor: Color,
  val titleContentColor: Color,
  val headlineContentColor: Color,
  val weekdayContentColor: Color,
  val subheadContentColor: Color,
  val navigationContentColor: Color,
  val yearContentColor: Color,
  val disabledYearContentColor: Color,
  val currentYearContentColor: Color,
  val selectedYearContentColor: Color,
  val disabledSelectedYearContentColor: Color,
  val selectedYearContainerColor: Color,
  val dayContentColor: Color,
  val disabledDayContentColor: Color,
  val dayInSelectionRangeContentColor: Color,
  val dividerColor: Color,
  val dateTextColor: Color,
  val dateTextContainerColor: Color,
)
