package com.hedvig.android.feature.editcoinsured.ui

fun formatSsn(ssn: String): String {
  return if (ssn.length == 12) {
    buildString {
      append(ssn.substring(0..7))
      append("-")
      append(ssn.substring(8..11))
    }
  } else {
    ssn
  }
}
