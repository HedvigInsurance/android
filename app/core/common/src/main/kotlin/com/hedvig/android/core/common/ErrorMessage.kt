package com.hedvig.android.core.common

data class ErrorMessage(
  val message: String? = null,
  val throwable: Throwable? = null,
)
