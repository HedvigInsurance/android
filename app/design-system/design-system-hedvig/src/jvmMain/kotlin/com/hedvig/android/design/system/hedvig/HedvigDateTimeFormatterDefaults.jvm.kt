package com.hedvig.android.design.system.hedvig

import com.hedvig.android.design.system.hedvig.api.CommonLocale
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime

/**
 * todo kmp: Move this from jvmMain and androidMain to a common jvmAndAndroidMain if it's possible
 * https://slack-chats.kotlinlang.org/t/32799394/i-got-a-kmp-hierarchy-like-this-and-i-am-interested-in-shari#1329acce-f196-4521-b0a1-d50770ff1101
 */
actual object HedvigDateTimeFormatterDefaults {
  /**
   * Example output: "2021.07.01"
   */
  actual fun isoLocalDateWithDots(locale: CommonLocale): DateFormatter {
    val formatter = DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('.')
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendLiteral('.')
      .appendValue(ChronoField.DAY_OF_MONTH, 2)
      .toFormatter(locale)
    return JavaDateFormatter(formatter)
  }

  /**
   * Example output: "12:34"
   */
  actual fun timeOnly(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "Fri 12:34"
   */
  actual fun dayOfTheWeekAndTime(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendPattern("EEE")
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "Nov 11 9:04"
   */
  actual fun monthDateAndTime(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendPattern("MMM")
        .appendLiteral(' ')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "11 Nov 2024"
   */
  actual fun dateMonthAndYear(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendPattern("MMM")
        .appendLiteral(' ')
        .appendValue(ChronoField.YEAR, 4)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "Nov 11 2024"
   */
  actual fun monthDateAndYear(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendPattern("MMM")
        .appendLiteral(' ')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendValue(ChronoField.YEAR, 4)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "2022 Nov 11 9:04"
   */
  actual fun yearMonthDateAndTime(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendPattern("yyyy")
        .appendLiteral(' ')
        .appendPattern("MMM")
        .appendLiteral(' ')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "16 Jan"
   */
  actual fun dateAndMonth(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendValue(ChronoField.DAY_OF_MONTH)
        .appendLiteral(' ')
        .appendPattern("MMM")
        .toFormatter(locale),
    )
  }

  /**
   * Example output: "910113"
   */
  actual fun yearMonthDayCombined(locale: CommonLocale): DateFormatter {
    return JavaDateFormatter(
      DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 2)
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .toFormatter(locale),
    )
  }
}

private class JavaDateFormatter(private val formatter: DateTimeFormatter) : DateFormatter {
  override fun format(date: LocalDate): String {
    return format(date.toJavaLocalDate())
  }

  override fun format(date: LocalDateTime): String {
    return format(date.toJavaLocalDateTime())
  }

  private fun format(temporalAccessor: TemporalAccessor): String {
    return formatter.format(temporalAccessor)
  }
}
