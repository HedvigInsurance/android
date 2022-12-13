package com.hedvig.android.auth.interceptor

import okhttp3.Request

internal fun Request.withAuthorizationToken(token: String): Request {
  return newBuilder().header("Authorization", token).build()
}
