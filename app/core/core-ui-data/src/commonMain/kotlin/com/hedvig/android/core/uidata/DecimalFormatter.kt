package com.hedvig.android.core.uidata

fun interface DecimalFormatter {
  fun format(number: Number): String
}

internal expect val decimalFormatter: DecimalFormatter

expect fun DecimalFormatter(pattern: String) : DecimalFormatter
