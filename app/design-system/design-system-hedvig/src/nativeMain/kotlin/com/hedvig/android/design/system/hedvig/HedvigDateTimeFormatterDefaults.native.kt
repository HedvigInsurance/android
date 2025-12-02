package com.hedvig.android.design.system.hedvig

import com.hedvig.android.design.system.hedvig.api.CommonLocale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// todo ios date formatting
actual object HedvigDateTimeFormatterDefaults {
  actual fun isoLocalDateWithDots(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun timeOnly(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun dayOfTheWeekAndTime(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun monthDateAndTime(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun dateMonthAndYear(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun monthDateAndYear(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun yearMonthDateAndTime(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun dateAndMonth(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }

  actual fun yearMonthDayCombined(locale: CommonLocale): DateFormatter {
    return IosDateFormatter()
  }
}

// todo ios date formatting
private class IosDateFormatter() : DateFormatter {
  override fun format(date: LocalDate): String {
    return date.toString()
  }

  override fun format(date: LocalDateTime): String {
    return date.toString()
  }
}
