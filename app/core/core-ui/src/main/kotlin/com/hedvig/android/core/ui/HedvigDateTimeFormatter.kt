package com.hedvig.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.format.DateTimeFormatter
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
fun hedvigNextPaymentDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("d MMM", locale)
}

/**
 * Example output: "910113"
 */
fun hedvigSecondaryBirthDateDateTimeFormatter(locale: Locale): DateTimeFormatter {
  return DateTimeFormatter.ofPattern("yyMMd", locale)
}

@Composable
fun rememberHedvigDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigDateTimeFormatter(locale) }
}

@Composable
fun rememberHedvigBirthDateDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigSecondaryBirthDateDateTimeFormatter(locale) }
}

@Composable
fun rememberHedvigNextPaymentDateTimeFormatter(): DateTimeFormatter {
  val locale = getLocale()
  return remember(locale) { hedvigNextPaymentDateTimeFormatter(locale) }
}
