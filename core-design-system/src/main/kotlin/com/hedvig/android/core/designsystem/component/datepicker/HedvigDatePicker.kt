package com.hedvig.android.core.designsystem.component.datepicker

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * Renders the M3 DatePicker but after cutting a bit from the top and the bottom to match our design. Particularly, we
 * do not need to render the confirmation/cancellation buttons at the bottom, nor the title at the top. This component
 * accommodates for both of these, while keeping the height taken to a minimum, while still maintaining good support for
 * bigger fonts.
 * Also matches colors better by using tertiary for the date selection by default.
 * To be used outside of dialogs, inline just like any other component.
 */
@Composable
fun HedvigDatePicker(
  datePickerState: DatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerFormatter() },
  dateValidator: (Long) -> Boolean = { true },
  colors: DatePickerColors = DatePickerDefaults.colors(
    selectedDayContainerColor = MaterialTheme.colorScheme.tertiary,
    selectedDayContentColor = MaterialTheme.colorScheme.onTertiary,
  ),
) {
  DatePicker(
    state = datePickerState,
    dateFormatter = dateFormatter,
    dateValidator = dateValidator,
    colors = colors,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewHedvigDatePickerSimple() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigDatePicker(
        rememberDatePickerState(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigDatePickerInput() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigDatePicker(
        rememberDatePickerState(initialDisplayMode = DisplayMode.Input),
      )
    }
  }
}
