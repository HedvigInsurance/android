package com.hedvig.android.core.common.android.validation

import androidx.annotation.StringRes

data class ValidationResult(
  val isSuccessful: Boolean,
  @StringRes val errorTextKey: Int?,
)
