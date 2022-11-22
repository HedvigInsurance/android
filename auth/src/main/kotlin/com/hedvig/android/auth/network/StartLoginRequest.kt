package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginMethod
import okhttp3.FormBody
import okhttp3.RequestBody

fun createStartLoginRequest(
  loginMethod: LoginMethod,
  market: String,
  personalNumber: String,
  email: String?,
): RequestBody {
  val builder = FormBody.Builder()

  builder.add("method", loginMethod.name)
  builder.add("country", market)
  builder.add("personalNumber", personalNumber)

  if (email != null) {
    builder.add("email", email)
  }

  return builder.build()
}
