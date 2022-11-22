package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthorizationCode
import okhttp3.FormBody
import okhttp3.RequestBody

fun FormBody.Builder.createSubmitAuthorizationCodeRequest(
  authorizationCode: AuthorizationCode,
): RequestBody {
  add("authorizationCode", authorizationCode.code)
  add("grant_type", "authorization_code")
  return build()
}
