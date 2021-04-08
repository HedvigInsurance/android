package com.hedvig.app.feature.embark

import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.app.util.whenApiVersion
import java.util.regex.Pattern

const val PERSONAL_NUMBER = "PersonalNumber"
const val SWEDISH_POSTAL_CODE = "PostalCode"
const val EMAIL = "Email"
const val BIRTH_DATE = "BirthDate"
const val BIRTH_DATE_REVERSE = "BirthDateReverse"
const val NORWEGIAN_POSTAL_CODE = "NorwegianPostalCode"

val PERSONAL_NUMBER_REGEX: Pattern = Pattern.compile("^\\d{6}-\\d{4}\$", Pattern.CASE_INSENSITIVE)
val SWEDISH_POSTAL_CODE_REGEX: Pattern = Pattern.compile("^\\d{3} \\d{2}$", Pattern.CASE_INSENSITIVE)
val EMAIL_REGEX: Pattern = Pattern.compile("^.+@.+\\..+\$", Pattern.CASE_INSENSITIVE)
val NORWEGIAN_POSTAL_CODE_REGEX: Pattern = Pattern.compile("^\\d{4}$", Pattern.CASE_INSENSITIVE)
val BIRTH_DATE_REGEX: Pattern = Pattern.compile("^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", Pattern.CASE_INSENSITIVE)
val BIRTH_DATE_REVERSE_REGEX: Pattern = Pattern.compile("^(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3}$", Pattern.CASE_INSENSITIVE)
val ANY_REGEX: Pattern = Pattern.compile("^.*\$", Pattern.CASE_INSENSITIVE)

fun validationCheck(mask: String, text: String) = when (mask) {
    PERSONAL_NUMBER -> PERSONAL_NUMBER_REGEX
    SWEDISH_POSTAL_CODE -> SWEDISH_POSTAL_CODE_REGEX
    EMAIL -> EMAIL_REGEX
    BIRTH_DATE -> BIRTH_DATE_REGEX
    BIRTH_DATE_REVERSE -> BIRTH_DATE_REVERSE_REGEX
    NORWEGIAN_POSTAL_CODE -> NORWEGIAN_POSTAL_CODE_REGEX
    else -> ANY_REGEX
}.matcher(text).find()

fun TextInputEditText.setInputType(mask: String) {
    if (mask == EMAIL) {
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        whenApiVersion(Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_EMAIL_ADDRESS)
        }
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
