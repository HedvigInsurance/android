package com.hedvig.android.auth.network

import com.hedvig.android.auth.ResendOtpResult
import okhttp3.Response

fun toResendOtpResult(response: Response): ResendOtpResult {
  val responseBody = response.body?.string()
  return if (response.isSuccessful && responseBody != null) {
    ResendOtpResult.Success
  } else {
    ResendOtpResult.Error(message = responseBody ?: "Unknown error")
  }
}
