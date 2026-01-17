package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.design.system.hedvig.datepicker.getLocale

@Composable
fun rememberHedvigDateTimeFormatter(): DateFormatter {
  val locale = getLocale()
  return remember(locale) { HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale) }
}

@Suppress("unused")
@Composable
fun rememberHedvigMonthDateTimeFormatter(): DateFormatter {
  val locale = getLocale()
  return remember(locale) { HedvigDateTimeFormatterDefaults.dateAndMonth(locale) }
}

@Suppress("unused")
@Composable
fun rememberHedvigBirthDateDateTimeFormatter(): DateFormatter {
  val locale = getLocale()
  return remember(locale) { HedvigDateTimeFormatterDefaults.yearMonthDayCombined(locale) }
}

expect object HedvigDateTimeFormatterDefaults {
  /**
   * Example output: "2021.07.01"
   */
  fun isoLocalDateWithDots(locale: CommonLocale): DateFormatter

  /**
   * Example output: "12:34"
   */
  fun timeOnly(locale: CommonLocale): DateFormatter

  /**
   * Example output: "Fri 12:34"
   */
  fun dayOfTheWeekAndTime(locale: CommonLocale): DateFormatter

  /**
   * Example output: "Nov 11 9:04"
   */
  fun monthDateAndTime(locale: CommonLocale): DateFormatter

  /**
   * Example output: "11 Nov 2024"
   */
  fun dateMonthAndYear(locale: CommonLocale): DateFormatter

  /**
   * Example output: "Nov 11 2024"
   */
  fun monthDateAndYear(locale: CommonLocale): DateFormatter

  /**
   * Example output: "2022 Nov 11 9:04"
   */
  fun yearMonthDateAndTime(locale: CommonLocale): DateFormatter

  /**
   * Example output: "16 Jan"
   */
  fun dateAndMonth(locale: CommonLocale): DateFormatter

  /**
   * Example output: "910113"
   */
  fun yearMonthDayCombined(locale: CommonLocale): DateFormatter
}
