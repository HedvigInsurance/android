package com.hedvig.android.core.ui

import androidx.annotation.StringRes

data class ValidatedInput<T>(
  val input: T,
  @StringRes
  val errorMessageRes: Int? = null,
) {
  val isPresent: Boolean
    get() = input != null
}
