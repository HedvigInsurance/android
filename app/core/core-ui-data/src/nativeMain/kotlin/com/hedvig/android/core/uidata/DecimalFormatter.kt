package com.hedvig.android.core.uidata

/**
 * TODO iOS: Consider implementing this for real if we have a need for it
 */
internal actual val decimalFormatter: DecimalFormatter = DecimalFormatter {
  it.toInt().toString()
}
