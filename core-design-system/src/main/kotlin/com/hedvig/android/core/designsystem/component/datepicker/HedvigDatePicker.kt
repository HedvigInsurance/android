package com.hedvig.android.core.designsystem.component.datepicker

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

/**
 * Renders the M3 DatePicker but after cutting a bit from the top and the bottom to match our design. Particularly, we
 * do not need to render the confirmation/cancellation buttons at the bottom, nor the title at the top. This component
 * accommodates for both of these, while keeping the height taken to a minimum, while still maintaining good support for
 * bigger fonts.
 * Also matches colors better by using tertiary for the date selection by default.
 * To be used outside of dialogs, inline just like any other component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HedvigDatePicker(
  datePickerState: DatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerFormatter() },
  dateValidator: (Long) -> Boolean = { true },
  headline: @Composable () -> Unit = {
    DatePickerDefaults.DatePickerHeadline(
      datePickerState,
      dateFormatter,
    )
  },
  colors: DatePickerColors = DatePickerDefaults.colors(
    selectedDayContainerColor = MaterialTheme.colorScheme.tertiary,
    selectedDayContentColor = MaterialTheme.colorScheme.onTertiary,
  ),
) {
  DatePicker(
    datePickerState = datePickerState,
    dateFormatter = dateFormatter,
    dateValidator = dateValidator,
    title = {
      // Explicitly set no text here, as we don't want it to render the default title but passing null is not
      // supported from the current implementation of m3 DatePicker
      Text("")
    },
    headline = headline,
    colors = colors,
    modifier = modifier
      .height(requiredDatePickerHeight)
      .layout { measurable, constraints ->
        val requiredHeight = datePickerRealHeight.roundToPx()
        val placeable = measurable.measure(
          constraints.copy(minHeight = requiredHeight, maxHeight = requiredHeight),
        )
        // If the layout size is bigger than incoming constraints, it will be centered in the parent. This finds
        // the real parent's top y position
        val actualZeroYPositionOfParent = (requiredHeight - constraints.maxHeight) / 2
        layout(placeable.width, placeable.height) {
          placeable.place(0, actualZeroYPositionOfParent - titleHeight.roundToPx())
        }
      },
  )
}

// Specs below taken from https://m3.material.io/components/date-pickers/specs#2a458311-c7a0-42c3-a517-495fe0e1f75e

// We do not want to render the "Select date" title so we can cut this much above. Still allows for room to render when
// on font scale x1.5
private val titleHeight = 36.dp

// We do not render the "Cancel" and "OK" buttons, so hide these too. Buttons + their padding
private val buttonsHeight = (36 + 8).dp

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/DatePickerModalTokens.kt;l=26
private val datePickerRealHeight = 568.dp

// The actual size of how much we want to render. The date picker, without the title and the buttons
private val requiredDatePickerHeight = datePickerRealHeight - titleHeight - buttonsHeight
