package com.hedvig.android.core.common.validation

import androidx.annotation.StringRes

data class ValidationResult(val isSuccessful: Boolean, @StringRes val errorTextKey: Int?)
