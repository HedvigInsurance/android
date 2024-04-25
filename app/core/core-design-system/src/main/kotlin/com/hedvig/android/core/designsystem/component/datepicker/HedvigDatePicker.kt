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
import com.hedvig.android.core.designsystem.material3.lightTypeContainer
import com.hedvig.android.core.designsystem.material3.onLightTypeContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock

/**
 * Matches colors better by using tertiary for the date selection by default.
 * To be used outside of dialogs, inline just like any other component.
 */
@Composable
fun HedvigDatePicker(
  datePickerState: DatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
  colors: DatePickerColors = DatePickerDefaults.colors(
    selectedDayContainerColor = MaterialTheme.colorScheme.lightTypeContainer,
    selectedDayContentColor = MaterialTheme.colorScheme.onLightTypeContainer,
    todayDateBorderColor = MaterialTheme.colorScheme.onTypeContainer,
  ),
) {
  DatePicker(
    state = datePickerState,
    dateFormatter = dateFormatter,
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
        rememberDatePickerState(initialSelectedDateMillis = Clock.System.now().minus(1.days).toEpochMilliseconds()),
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
