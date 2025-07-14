package com.hedvig.android.core.uidata

import java.text.DecimalFormat

internal actual val decimalFormatter: DecimalFormatter = DecimalFormatter {
  decimalFormat.format(it)
}

private val decimalFormat: DecimalFormat = DecimalFormat("")
