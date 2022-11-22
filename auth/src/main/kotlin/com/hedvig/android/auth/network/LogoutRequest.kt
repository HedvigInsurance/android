package com.hedvig.android.auth.network

import com.hedvig.android.auth.RefreshCode
import okhttp3.FormBody

fun createLogoutRequestBody(refreshCode: RefreshCode): FormBody {
  val builder = FormBody.Builder()
  builder.add("refresh_token", refreshCode.code)
  return builder.build()
}
