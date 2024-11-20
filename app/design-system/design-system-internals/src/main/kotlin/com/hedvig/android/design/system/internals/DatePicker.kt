package com.hedvig.android.design.system.internals

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import java.util.Locale
import kotlinx.coroutines.flow.drop

@Composable
fun rememberHedvigDatePickerState(
  initialSelectedDateMillis: Long,
  initialDisplayedMonthMillis: Long,
  yearRange: IntRange,
  initialDisplayMode: HedvigDisplayMode,
  selectableDates: HedvigSelectableDates,
): HedvigDatePickerState {
  val materialState = rememberDatePickerState(
    initialSelectedDateMillis,
    initialDisplayedMonthMillis,
    yearRange,
    initialDisplayMode.toMaterialDisplayMode(),
    selectableDates.toMaterialSelectableDates(),
  )
  return remember(materialState) { HedvigDatePickerStateImpl(materialState) }
}

fun HedvigDatePickerState(
  locale: Locale,
  initialSelectedDateMillis: Long?,
  initialDisplayedMonthMillis: Long?,
  yearRange: IntRange,
  initialDisplayMode: HedvigDisplayMode,
  selectableDates: HedvigSelectableDates,
): HedvigDatePickerState {
  val materialState = DatePickerState(
    locale = locale,
    initialSelectedDateMillis = initialSelectedDateMillis,
    initialDisplayedMonthMillis = initialDisplayedMonthMillis,
    yearRange = yearRange,
    initialDisplayMode = initialDisplayMode.toMaterialDisplayMode(),
    selectableDates = selectableDates.toMaterialSelectableDates(),
  )
  return HedvigDatePickerStateImpl(materialState)
}

@Composable
fun HedvigDatePicker(
  state: HedvigDatePickerState,
  onDismissRequest: () -> Unit,
  hedvigDatePickerColors: HedvigDatePickerColors,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigDatePicker(
    state = MaterialDatePickerStateImpl(state),
    onDismissRequest = onDismissRequest,
    hedvigDatePickerColors = hedvigDatePickerColors,
    confirmButton = confirmButton,
    modifier = modifier,
  )
}

@Composable
fun HedvigDatePicker(
  selectedDateMillis: Long?,
  displayedMonthMillis: Long?,
  yearRange: IntRange,
  minDateInMillis: Long,
  maxDateInMillis: Long,
  onDismissRequest: () -> Unit,
  onSelectedDateChanged: (Long?) -> Unit,
  hedvigDatePickerColors: HedvigDatePickerColors,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = rememberDatePickerState(
    initialSelectedDateMillis = selectedDateMillis,
    initialDisplayedMonthMillis = displayedMonthMillis,
    yearRange = yearRange,
    selectableDates = SelectableDatesImmutableImpl(
      minDateInMillis = minDateInMillis,
      maxDateInMillis = maxDateInMillis,
      yearRange = yearRange,
    ),
  )
  LaunchedEffect(state) {
    snapshotFlow { state.selectedDateMillis }
      .drop(1)
      .collect {
        onSelectedDateChanged(it)
      }
  }
  HedvigDatePicker(
    state = state,
    onDismissRequest = onDismissRequest,
    hedvigDatePickerColors = hedvigDatePickerColors,
    confirmButton = confirmButton,
    modifier = modifier,
  )
}

@Composable
private fun HedvigDatePicker(
  state: DatePickerState,
  onDismissRequest: () -> Unit,
  hedvigDatePickerColors: HedvigDatePickerColors,
  confirmButton: @Composable () -> Unit,
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
  DatePickerDialog(
    onDismissRequest = onDismissRequest,
    colors = hedvigColors,
    confirmButton = confirmButton,
  ) {
    HedvigDatePicker(
      modifier = modifier,
      datePickerState = state,
      colors = hedvigColors,
    )
  }
}

@Composable
private fun HedvigDatePicker(
  datePickerState: DatePickerState,
  colors: DatePickerColors,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
) {
  DatePicker(
    state = datePickerState,
    dateFormatter = dateFormatter,
    colors = colors,
    modifier = modifier,
  )
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

@Stable
private data class SelectableDatesImmutableImpl(
  val minDateInMillis: Long,
  val maxDateInMillis: Long,
  val yearRange: IntRange,
) : SelectableDates {
  override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minDateInMillis..maxDateInMillis

  override fun isSelectableYear(year: Int): Boolean = year in yearRange
}

private class HedvigDatePickerStateImpl(
  val materialState: DatePickerState,
) : HedvigDatePickerState {
  override var selectedDateMillis: Long?
    set(value) {
      materialState.selectedDateMillis = value
    }
    get() = materialState.selectedDateMillis

  override var displayedMonthMillis: Long
    get() = materialState.displayedMonthMillis
    set(value) {
      materialState.displayedMonthMillis = value
    }

  override var displayMode: HedvigDisplayMode
    get() = materialState.displayMode.toHedvigDisplayMode()
    set(value) {
      materialState.displayMode = value.toMaterialDisplayMode()
    }

  override val yearRange: IntRange
    get() = materialState.yearRange

  override val selectableDates: HedvigSelectableDates = materialState.selectableDates.toHedvigSelectableDates()
}

private class MaterialDatePickerStateImpl(
  val hedvigState: HedvigDatePickerState,
) : DatePickerState {
  override var selectedDateMillis: Long?
    get() = hedvigState.selectedDateMillis
    set(value) {
      hedvigState.selectedDateMillis = value
    }
  override var displayedMonthMillis: Long
    get() = hedvigState.displayedMonthMillis
    set(value) {
      hedvigState.displayedMonthMillis = value
    }
  override var displayMode: DisplayMode
    get() = hedvigState.displayMode.toMaterialDisplayMode()
    set(value) {
      hedvigState.displayMode = value.toHedvigDisplayMode()
    }
  override val yearRange: IntRange
    get() = hedvigState.yearRange
  override val selectableDates: SelectableDates
    get() = hedvigState.selectableDates.toMaterialSelectableDates()
}

private fun HedvigDisplayMode.toMaterialDisplayMode() = when (this) {
  HedvigDisplayMode.Picker -> DisplayMode.Picker
  HedvigDisplayMode.Input -> DisplayMode.Input
  else -> DisplayMode.Picker
}

private fun DisplayMode.toHedvigDisplayMode() = when (this) {
  DisplayMode.Picker -> HedvigDisplayMode.Picker
  DisplayMode.Input -> HedvigDisplayMode.Input
  else -> HedvigDisplayMode.Picker
}

private fun HedvigSelectableDates.toMaterialSelectableDates(): SelectableDates {
  return object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
      return this@toMaterialSelectableDates.isSelectableDate(utcTimeMillis)
    }

    override fun isSelectableYear(year: Int): Boolean {
      return this@toMaterialSelectableDates.isSelectableYear(year)
    }
  }
}

private fun SelectableDates.toHedvigSelectableDates(): HedvigSelectableDates {
  return object : HedvigSelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
      return this@toHedvigSelectableDates.isSelectableDate(utcTimeMillis)
    }

    override fun isSelectableYear(year: Int): Boolean {
      return this@toHedvigSelectableDates.isSelectableYear(year)
    }
  }
}
