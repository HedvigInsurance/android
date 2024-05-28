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

fun formatShortSsn(ssn: String): String {
  val formattedSSN = ssn.replace("-", "")
  if (formattedSSN.length == 10) {
    val ssnLastTwoDigitsOfYear = formattedSSN.substring(0, 2)
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val firstTwoDigitsOfTheYear = currentYear / 100
    val lastTwoDigitsOfTheYear = currentYear % 100

    val ssnLastTwoDigits = ssnLastTwoDigitsOfYear.toIntOrNull()

    return if (ssnLastTwoDigits != null && ssnLastTwoDigits > lastTwoDigitsOfTheYear) {
      "${firstTwoDigitsOfTheYear - 1}$ssn"
    } else {
      "$firstTwoDigitsOfTheYear$ssn"
    }
  } else {
    return ssn
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
