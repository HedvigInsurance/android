package com.hedvig.android.design.system.hedvig.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
interface HedvigDatePickerState {
  var selectedDateMillis: Long?
  var displayedMonthMillis: Long
  var displayMode: HedvigDisplayMode
  val yearRange: IntRange
  val selectableDates: HedvigSelectableDates
}

@Stable
interface HedvigSelectableDates {
  fun isSelectableDate(utcTimeMillis: Long) = true

  fun isSelectableYear(year: Int) = true
}

@Immutable
@JvmInline
value class HedvigDisplayMode internal constructor(internal val value: Int) {
  companion object {
    /** Date picker mode */
    val Picker = HedvigDisplayMode(0)

    /** Date text input mode */
    val Input = HedvigDisplayMode(1)
  }

  override fun toString() = when (this) {
    Picker -> "Picker"
    Input -> "Input"
    else -> "Unknown"
  }
}

object HedvigDatePickerDefaults {
  val displayMode = HedvigDisplayMode.Picker
  val YearRange: IntRange = IntRange(1900, 2100)
  val AllDates: HedvigSelectableDates = object : HedvigSelectableDates {}
}
