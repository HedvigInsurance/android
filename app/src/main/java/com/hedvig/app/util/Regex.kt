package com.hedvig.app.util

import java.util.regex.Pattern

val EMAIL_REGEX: Pattern = Pattern.compile("^.+@.+\\..+\$", Pattern.CASE_INSENSITIVE)

val SWEDISH_PERSONAL_NUMBER_REGEX: Pattern = Pattern.compile("^\\d{6}-\\d{4}\$", Pattern.CASE_INSENSITIVE)
val DANISH_PERSONAL_NUMBER_REGEX: Pattern = Pattern.compile("^\\d{6}-\\d{4}\$", Pattern.CASE_INSENSITIVE)
val NORWEGIAN_PERSONAL_NUMBER_REGEX: Pattern = Pattern.compile("^\\d{6}-\\d{5}\$", Pattern.CASE_INSENSITIVE)
val SWEDISH_POSTAL_CODE_REGEX: Pattern = Pattern.compile("^\\d{3} \\d{2}$", Pattern.CASE_INSENSITIVE)
val NORWEGIAN_POSTAL_CODE_REGEX: Pattern = Pattern.compile("^\\d{4}$", Pattern.CASE_INSENSITIVE)
val BIRTH_DATE_REGEX: Pattern = Pattern.compile(
    "^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
    Pattern.CASE_INSENSITIVE,
)
val BIRTH_DATE_REVERSE_REGEX: Pattern = Pattern.compile(
    "^(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3}$",
    Pattern.CASE_INSENSITIVE,
)
val ANY_REGEX: Pattern = Pattern.compile("^.*\$", Pattern.CASE_INSENSITIVE)
