package com.hedvig.app.feature.embark

import android.text.InputType
import java.util.regex.Pattern

const val PERSONAL_NUMBER = "PersonalNumber"
const val SWEDISH_POSTAL_CODE = "PostalCode"
const val EMAIL = "Email"
const val BIRTH_DATE = "BirthDate"
const val BIRTH_DATE_REVERSE = "BirthDateReverse"
const val NORWEGIAN_POSTAL_CODE = "NorwegianPostalCode"

const val PERSONAL_NUMBER_REGEX = "^\\d{6}\\d{4}$"
const val SWEDISH_POSTAL_CODE_REGEX = "^\\d{3}\\d{2}$"
const val EMAIL_REGEX = "^.+@.+\\..+\$"
const val NORWEGIAN_POSTAL_CODE_REGEX = "^\\d{4}$"
const val BIRTH_DATE_REGEX = "^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
const val BIRTH_DATE_REVERSE_REGEX = "^(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3}$"

fun validationCheck(mask: String, text: String) = Pattern.compile(
    when (mask) {
        PERSONAL_NUMBER -> PERSONAL_NUMBER_REGEX
        SWEDISH_POSTAL_CODE -> SWEDISH_POSTAL_CODE_REGEX
        EMAIL -> EMAIL_REGEX
        BIRTH_DATE -> BIRTH_DATE_REGEX
        BIRTH_DATE_REVERSE -> BIRTH_DATE_REVERSE_REGEX
        NORWEGIAN_POSTAL_CODE -> NORWEGIAN_POSTAL_CODE_REGEX
        else -> ""
    }, Pattern.CASE_INSENSITIVE
).matcher(text).find()

fun getInputType(mask: String) = when (mask) {
    PERSONAL_NUMBER,
    SWEDISH_POSTAL_CODE,
    BIRTH_DATE,
    BIRTH_DATE_REVERSE,
    NORWEGIAN_POSTAL_CODE -> InputType.TYPE_CLASS_NUMBER
    EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    else -> 0
}
