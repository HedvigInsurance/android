package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginMethod
import okhttp3.FormBody
import okhttp3.RequestBody

fun FormBody.Builder.createStartLoginRequest(
  loginMethod: LoginMethod,
  market: String,
  personalNumber: String,
  email: String?,
): RequestBody {
  add("method", loginMethod.name)
  add("country", market)
  add("personalNumber", personalNumber)

  if (email != null) {
    add("email", email)
  }

  return build()
}
