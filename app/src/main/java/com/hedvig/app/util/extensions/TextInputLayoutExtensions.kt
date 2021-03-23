package com.hedvig.app.util.extensions

import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.setHelperText(@StringRes resId: Int) {
    helperText = resources.getString(resId)
}
