package com.hedvig.android.core.uidata

internal fun interface DecimalFormatter {
  fun format(number: Double): String
}

internal expect val decimalFormatter: DecimalFormatter
