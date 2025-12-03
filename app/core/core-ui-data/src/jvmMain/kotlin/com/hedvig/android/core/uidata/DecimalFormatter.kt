package com.hedvig.android.core.uidata

import java.text.DecimalFormat

internal actual val decimalFormatter: DecimalFormatter = DecimalFormatter {
  decimalFormat.format(it)
}

actual fun DecimalFormatter(pattern: String) : DecimalFormatter {
  val javaDecimalFormat = DecimalFormat(pattern)
  return DecimalFormatter {
    javaDecimalFormat.format(it)
  }
}

private val decimalFormat: DecimalFormat = DecimalFormat("")
