package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerDefaults
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.fromToken
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextNegative
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextTertiary
import com.hedvig.android.design.system.internals.HedvigDatePicker
import com.hedvig.android.design.system.internals.HedvigDatePickerColors
import hedvig.resources.R
import java.util.Locale

@Composable
fun rememberHedvigDatePickerState(
  initialSelectedDateMillis: Long,
  initialDisplayedMonthMillis: Long,
  yearRange: IntRange,
  initialDisplayMode: HedvigDisplayMode,
  selectableDates: HedvigSelectableDates,
): HedvigDatePickerState {
  return com.hedvig.android.design.system.internals.rememberHedvigDatePickerState(
    initialSelectedDateMillis,
    initialDisplayedMonthMillis,
    yearRange,
    initialDisplayMode,
    selectableDates,
  )
}

fun HedvigDatePickerState(
  locale: Locale,
  initialSelectedDateMillis: Long?,
  initialDisplayedMonthMillis: Long?,
  yearRange: IntRange = HedvigDatePickerDefaults.YearRange,
  initialDisplayMode: HedvigDisplayMode = HedvigDatePickerDefaults.displayMode,
  selectableDates: HedvigSelectableDates = HedvigDatePickerDefaults.AllDates,
): HedvigDatePickerState {
  return com.hedvig.android.design.system.internals.HedvigDatePickerState(
    locale,
    initialSelectedDateMillis,
    initialDisplayedMonthMillis,
    yearRange,
    initialDisplayMode,
    selectableDates,
  )
}

data class HedvigDatePickerImmutableState(
  val selectedDateMillis: Long?,
  val displayedMonthMillis: Long?,
  val yearRange: IntRange,
  val minDateInMillis: Long,
  val maxDateInMillis: Long,
  val locale: Locale,
)

@Composable
fun HedvigDatePicker(
  datePickerState: HedvigDatePickerState,
  onDismissRequest: () -> Unit,
  onConfirmRequest: () -> Unit,
) {
  HedvigDatePicker(
    state = datePickerState,
    onDismissRequest = onDismissRequest,
    hedvigDatePickerColors = hedvigDatePickerColors,
    modifier = Modifier.background(
      hedvigDatePickerColors.containerColor,
    ),
    confirmButton = {
      HedvigTextButton(
        text = stringResource(R.string.general_done_button),
        onClick = onConfirmRequest,
        buttonSize = Medium,
      )
    },
  )
}

/**
 * A version of [HedvigDatePicker] which does not offer internal mutability in the [datePickerState] parameter. Instead
 * it takes in an immutable data class and it must be re-created again every time something changes
 */
@Composable
fun HedvigDatePicker(
  datePickerState: HedvigDatePickerImmutableState,
  onDismissRequest: () -> Unit,
  onConfirmRequest: () -> Unit,
  onSelectedDateChanged: (Long?) -> Unit,
  isVisible: Boolean,
) {
  if (isVisible) {
    with(datePickerState) {
      HedvigDatePicker(
        selectedDateMillis = selectedDateMillis,
        displayedMonthMillis = displayedMonthMillis,
        yearRange = yearRange,
        minDateInMillis = minDateInMillis,
        maxDateInMillis = maxDateInMillis,
        onDismissRequest = onDismissRequest,
        hedvigDatePickerColors = hedvigDatePickerColors,
        onSelectedDateChanged = onSelectedDateChanged,
        modifier = Modifier.background(
          hedvigDatePickerColors.containerColor,
        ),
        confirmButton = {
          HedvigTextButton(
            text = stringResource(R.string.general_save_button),
            onClick = onConfirmRequest,
            buttonSize = Medium,
          )
        },
      )
    }
  }
}

private val hedvigDatePickerColors: HedvigDatePickerColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      HedvigDatePickerColors(
        containerColor = fromToken(ColorSchemeKeyTokens.BackgroundPrimary),
        selectedDayContainerColor = fromToken(ColorSchemeKeyTokens.SignalGreenElement),
        selectedDayContentColor = fromToken(ColorSchemeKeyTokens.TextWhite),
        todayContentColor = fromToken(ColorSchemeKeyTokens.SignalGreenElement),
        todayDateBorderColor = fromToken(ColorSchemeKeyTokens.SignalGreenElement),
        textButtonColor = fromToken(TextPrimary),
        titleContentColor = fromToken(TextPrimary),
        headlineContentColor = fromToken(TextPrimary),
        weekdayContentColor = fromToken(TextTertiary),
        subheadContentColor = fromToken(TextPrimary),
        navigationContentColor = fromToken(TextPrimary),
        yearContentColor = fromToken(TextPrimary),
        disabledYearContentColor = fromToken(TextTertiary),
        currentYearContentColor = fromToken(TextPrimary),
        selectedYearContentColor = fromToken(TextNegative),
        disabledSelectedYearContentColor = fromToken(TextTertiary),
        selectedYearContainerColor = fromToken(TextPrimary),
        dayContentColor = fromToken(TextPrimary),
        disabledDayContentColor = fromToken(TextTertiary),
        dayInSelectionRangeContentColor = fromToken(TextPrimary),
        dividerColor = fromToken(TextTertiary),
        dateTextColor = fromToken(TextPrimary),
        dateTextContainerColor = Color.Transparent,
      )
    }
  }
