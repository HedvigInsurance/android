package com.hedvig.android.core.uidata

import com.hedvig.android.core.locale.AppFormattingLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

internal actual val decimalFormatter: DecimalFormatter = DecimalFormatter { number ->
  val locale = AppFormattingLocale.current ?: Locale.getDefault()
  synchronized(decimalFormats) {
    decimalFormats.getOrPut(locale) { DecimalFormat("", DecimalFormatSymbols.getInstance(locale)) }.format(number)
  }
}

actual fun DecimalFormatter(pattern: String): DecimalFormatter {
  val javaDecimalFormat = DecimalFormat(pattern)
  return DecimalFormatter {
    javaDecimalFormat.format(it)
  }
}

private val decimalFormats = mutableMapOf<Locale, DecimalFormat>()
