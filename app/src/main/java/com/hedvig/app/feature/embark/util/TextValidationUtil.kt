package com.hedvig.app.feature.embark.util

import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.app.util.whenApiVersion

fun TextInputEditText.setInputType(mask: MaskType) {
    if (mask == MaskType.EMAIL) {
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        whenApiVersion(Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_EMAIL_ADDRESS)
        }
    } else if (mask == MaskType.BIRTH_DATE ||
        mask == MaskType.BIRTH_DATE_REVERSE ||
        mask == MaskType.PERSONAL_NUMBER ||
        mask == MaskType.POSTAL_CODE ||
        mask == MaskType.NORWEGIAN_POSTAL_CODE
    ) {
        keyListener = DigitsKeyListener.getInstance(
            when (mask) {
                MaskType.PERSONAL_NUMBER,
                MaskType.BIRTH_DATE,
                MaskType.BIRTH_DATE_REVERSE -> "0123456789-"
                MaskType.NORWEGIAN_POSTAL_CODE -> "0123456789"
                MaskType.POSTAL_CODE -> "0123456789 "
                else -> "0123456789- "
            }
        )
    }
}

fun TextInputEditText.setValidationFormatter(mask: MaskType) {
    if (mask == MaskType.BIRTH_DATE ||
        mask == MaskType.BIRTH_DATE_REVERSE ||
        mask == MaskType.PERSONAL_NUMBER ||
        mask == MaskType.POSTAL_CODE ||
        mask == MaskType.NORWEGIAN_POSTAL_CODE
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
                    MaskType.PERSONAL_NUMBER -> {
                        if (prevLength < length && length == 6) {
                            editable.append("-")
                        }
                    }
                    MaskType.POSTAL_CODE -> {
                        if (prevLength < length && length == 3) {
                            editable.append(" ")
                        }
                    }
                    MaskType.BIRTH_DATE -> {
                        if (prevLength < length && (length == 4 || length == 7)) {
                            editable.append("-")
                        }
                    }
                    MaskType.BIRTH_DATE_REVERSE -> {
                        if (prevLength < length && (length == 2 || length == 5)) {
                            editable.append("-")
                        }
                    }
                }
            }
        })
    }
}
