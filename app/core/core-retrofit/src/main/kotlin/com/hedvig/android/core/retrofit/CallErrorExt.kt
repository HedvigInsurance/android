package com.hedvig.android.core.retrofit

import arrow.retrofit.adapter.either.networkhandling.CallError
import arrow.retrofit.adapter.either.networkhandling.HttpError
import arrow.retrofit.adapter.either.networkhandling.IOError
import arrow.retrofit.adapter.either.networkhandling.UnexpectedCallError
import com.hedvig.android.core.common.ErrorMessage

fun CallError.toErrorMessage(): ErrorMessage {
  val callError = this
  return ErrorMessage(
    message = when (callError) {
      is HttpError -> "Code:${callError.code}. Message:${callError.message} Body:${callError.body}"
      is IOError -> callError.cause.localizedMessage
      is UnexpectedCallError -> callError.cause.localizedMessage
    },
    throwable = when (callError) {
      is HttpError -> null
      is IOError -> callError.cause
      is UnexpectedCallError -> callError.cause
    },
  )
}
