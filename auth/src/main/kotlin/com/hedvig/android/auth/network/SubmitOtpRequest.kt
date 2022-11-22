package com.hedvig.android.auth.network

import okhttp3.FormBody
import okhttp3.RequestBody

fun createSubmitOtpRequest(
  otp: String,
): RequestBody {
  val builder = FormBody.Builder()
  builder.add("otp", otp)
  return builder.build()
}

