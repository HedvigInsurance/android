package com.hedvig.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * A formatter which emulates the [DateTimeFormatter.ISO_LOCAL_DATE] but replaces the '-' dashes with '.' dots.
 * Example output: "2021.07.01"
 */
fun hedvigDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral('.')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('.')
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .toFormatter(locale)
}

/**
 * Example output: "May 26 2023"
 */
fun hedvigSecondaryDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("MMMM d yyyy", locale)
}

@Composable
fun rememberHedvigDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigDateTimeFormatter(locale) }
}
