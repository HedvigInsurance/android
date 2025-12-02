package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hedvig.android.design.system.hedvig.api.CommonLocale
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
  fun isoLocalDateWithDots(locale: CommonLocale): DateFormatter

  fun timeOnly(locale: CommonLocale): DateFormatter

  fun dayOfTheWeekAndTime(locale: CommonLocale): DateFormatter

  fun monthDateAndTime(locale: CommonLocale): DateFormatter

  fun dateMonthAndYear(locale: CommonLocale): DateFormatter

  fun monthDateAndYear(locale: CommonLocale): DateFormatter

  fun yearMonthDateAndTime(locale: CommonLocale): DateFormatter

  fun dateAndMonth(locale: CommonLocale): DateFormatter

  fun yearMonthDayCombined(locale: CommonLocale): DateFormatter
}
