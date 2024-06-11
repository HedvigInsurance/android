package com.hedvig.android.core.common

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

fun formatName(firstName: String?, lastName: String?): String {
  return buildString {
    if (firstName != null) {
      append(firstName)
    }
    if (firstName != null && lastName != null) {
      append(" ")
    }
    if (lastName != null) {
      append(lastName)
    }
  }
}
