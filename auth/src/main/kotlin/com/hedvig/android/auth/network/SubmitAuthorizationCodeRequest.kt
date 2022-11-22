package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthorizationCode
import com.hedvig.android.auth.LoginAuthorizationCode
import com.hedvig.android.auth.RefreshCode
import okhttp3.FormBody

fun AuthorizationCode.createRequestBody(): FormBody {

  val builder = FormBody.Builder()

  when (this) {
    is LoginAuthorizationCode -> {
      builder.add("authorizationCode", code)
      builder.add("grant_type", "authorization_code")
    }
    is RefreshCode -> {
      builder.add("refresh_token", code)
      builder.add("grant_type", "refresh_token")
    }
  }

  return builder.build()
}
