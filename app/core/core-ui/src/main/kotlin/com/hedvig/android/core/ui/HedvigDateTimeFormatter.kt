package com.hedvig.android.core.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * Example output: "16 Jan 2023"
 */
fun hedvigDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("d MMM yyyy", locale)
}

/**
 * Example output: "16 Jan"
 */
fun hedvigMonthDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("d MMM", locale)
}

/**
 * Example output: "910113"
 */
fun hedvigSecondaryBirthDateDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("yyMMdd", locale)
}

@Composable
fun rememberHedvigDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigDateTimeFormatter(locale) }
}

@Composable
fun rememberHedvigMonthDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigMonthDateTimeFormatter(locale) }
}

@Composable
fun rememberHedvigBirthDateDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigSecondaryBirthDateDateTimeFormatter(locale) }
}

// todo, migrate the above into [HedvigDateTimeFormatterDefaults]
object HedvigDateTimeFormatterDefaults {
  fun isoLocalDateWithDots(locale: Locale): DateTimeFormatter {
    return isoLocalDateWithDots.toFormatter(locale)
  }

  fun timeOnly(locale: Locale): DateTimeFormatter {
    return timeOnly.toFormatter(locale)
  }

  fun dayOfTheWeekAndTime(locale: Locale): DateTimeFormatter {
    return dayOfTheWeekAndTime.toFormatter(locale)
  }

  fun monthDateAndTime(locale: Locale): DateTimeFormatter {
    return monthDateAndTime.toFormatter(locale)
  }

  fun monthDateAndYear(locale: Locale): DateTimeFormatter {
    return monthDateAndYear.toFormatter(locale)
  }

  fun yearMonthDateAndTime(locale: Locale): DateTimeFormatter {
    return yearMonthDateAndTime.toFormatter(locale)
  }
}

/**
 * Example output: "2021.07.01"
 */
@SuppressLint("NewApi") // We do have desugaring enabled
private val isoLocalDateWithDots: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
  .appendLiteral('.')
  .appendValue(ChronoField.MONTH_OF_YEAR, 2)
  .appendLiteral('.')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)

/**
 * Example output: "12:34"
 */
@SuppressLint("NewApi")
private val timeOnly: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "Fri 12:34"
 */
@SuppressLint("NewApi")
private val dayOfTheWeekAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendPattern("EEE")
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "Nov 11 9:04"
 */
@SuppressLint("NewApi")
private val monthDateAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder() // .appendPattern("MMM dd HH:mm")
  .appendPattern("MMM")
  .appendLiteral(' ')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)

/**
 * Example output: "Nov 11 2024"
 */
@SuppressLint("NewApi")
private val monthDateAndYear: DateTimeFormatterBuilder = DateTimeFormatterBuilder() // .appendPattern("MMM dd HH:mm")
  .appendPattern("MMM")
  .appendLiteral(' ')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(ChronoField.YEAR, 4)

/**
 * Example output: "2022 Nov 11 9:04"
 */
@SuppressLint("NewApi")
private val yearMonthDateAndTime: DateTimeFormatterBuilder = DateTimeFormatterBuilder()
  .appendPattern("yyyy")
  .appendLiteral(' ')
  .appendPattern("MMM")
  .appendLiteral(' ')
  .appendValue(ChronoField.DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(ChronoField.HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
