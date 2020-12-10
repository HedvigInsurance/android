package com.hedvig.app.feature.embark

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import com.google.android.material.textfield.TextInputEditText
import java.util.regex.Pattern

const val PERSONAL_NUMBER = "PersonalNumber"
const val SWEDISH_POSTAL_CODE = "PostalCode"
const val EMAIL = "Email"
const val BIRTH_DATE = "BirthDate"
const val BIRTH_DATE_REVERSE = "BirthDateReverse"
const val NORWEGIAN_POSTAL_CODE = "NorwegianPostalCode"

const val PERSONAL_NUMBER_REGEX = "^\\d{6}-\\d{4}\$"
const val SWEDISH_POSTAL_CODE_REGEX = "^\\d{3} \\d{2}$"
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

fun TextInputEditText.setInputType(mask: String) {
    if (mask == EMAIL) {
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    } else if (mask == BIRTH_DATE ||
        mask == BIRTH_DATE_REVERSE ||
        mask == PERSONAL_NUMBER ||
        mask == SWEDISH_POSTAL_CODE ||
        mask == NORWEGIAN_POSTAL_CODE
    ) {
        keyListener = DigitsKeyListener.getInstance(
            when (mask) {
                PERSONAL_NUMBER,
                BIRTH_DATE,
                BIRTH_DATE_REVERSE -> "0123456789-"
                NORWEGIAN_POSTAL_CODE -> "0123456789"
                SWEDISH_POSTAL_CODE -> "0123456789 "
                else -> "0123456789- "
            }
        )
    }
}

fun TextInputEditText.setValidationFormatter(mask: String) {
    if (mask == BIRTH_DATE ||
        mask == BIRTH_DATE_REVERSE ||
        mask == PERSONAL_NUMBER ||
        mask == SWEDISH_POSTAL_CODE ||
        mask == NORWEGIAN_POSTAL_CODE
    ) {
        var prevLength = 0
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                prevLength = text.toString().length
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                val length = editable.length
                when (mask) {
                    PERSONAL_NUMBER -> {
                        if (prevLength < length && length == 6) {
                            editable.append("-")
                        }
                    }
                    SWEDISH_POSTAL_CODE -> {
                        if (prevLength < length && length == 3) {
                            editable.append(" ")
                        }
                    }
                    BIRTH_DATE -> {
                        if (prevLength < length && (length == 4 || length == 7)) {
                            editable.append("-")
                        }
                    }
                    BIRTH_DATE_REVERSE -> {
                        if (prevLength < length && (length == 2 || length == 5)) {
                            editable.append("-")
                        }
                    }
                }
            }
        })
    }
}
