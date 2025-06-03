package com.hedvig.authlib.authservice

import com.hedvig.authlib.AuthEnvironment
import com.hedvig.authlib.authservice.model.*
import com.hedvig.authlib.baseUrl
import com.hedvig.authlib.url.LoginStatusUrl
import com.hedvig.authlib.url.OtpResendUrl
import com.hedvig.authlib.url.OtpVerifyUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Service mapping the API of `HedvigInsurance/auth`
 */
internal class AuthService(
  private val environment: AuthEnvironment,
  private val ktorClient: HttpClient,
) {
  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun memberLoginSweden(personalNumber: String?): LoginSwedenResponse {
    val response = ktorClient.post("${environment.baseUrl}/member-login") {
      headers["hedvig-bankid-v6"] = "true"
      contentType(ContentType.Application.Json)
      setBody(LoginSwedenInput(personalNumber))
    }
    return response.body<LoginSwedenResponse>()
  }

  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun memberLoginOtp(
    otpLoginCountry: LoginOtpInput.OtpLoginCountry,
    personalNumber: String,
  ): LoginOtpResponse {
    val response = ktorClient.post("${environment.baseUrl}/member-login") {
      contentType(ContentType.Application.Json)
      setBody(LoginOtpInput(otpLoginCountry, personalNumber))
    }
    return response.body<LoginOtpResponse>()
  }

  /**
   * Used for Qasa login attempts
   */
  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun memberLoginOtpSweden(email: String): LoginOtpResponse {
    val response = ktorClient.post("${environment.baseUrl}/member-login") {
      contentType(ContentType.Application.Json)
      setBody(LoginOtpSwedenInput(email))
    }
    return response.body<LoginOtpResponse>()
  }

  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun loginStatus(loginStatusUrl: LoginStatusUrl): LoginStatusResponse {
    val response = ktorClient.get("${environment.baseUrl}${loginStatusUrl.url}")
    return response.body<LoginStatusResponse>()
  }

  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun otpVerify(otp: String, otpVerifyUrl: OtpVerifyUrl): OtpVerifyResponse {
    val response = ktorClient.post("${environment.baseUrl}${otpVerifyUrl.url}") {
      contentType(ContentType.Application.Json)
      setBody(OtpVerifyInput(otp))
    }
    return response.body<OtpVerifyResponse>()
  }

  /**
   * @return [true] if the request had a status 200 as a response
   */
  @Throws(Throwable::class)
  suspend fun otpResend(otpResendUrl: OtpResendUrl): Boolean {
    val response = ktorClient.post("${environment.baseUrl}${otpResendUrl.url}")
    return response.status == HttpStatusCode.OK
  }

  @Throws(NoTransformationFoundException::class, Throwable::class)
  suspend fun grantToken(grantTokenInput: GrantTokenInput): GrantTokenOutput {
    val response = ktorClient.post("${environment.baseUrl}/oauth/token") {
      contentType(ContentType.Application.Json)
      setBody(grantTokenInput)
    }
    return response.body<GrantTokenOutput>()
  }

  /**
   * @return [true] if the request had a status 200 as a response
   */
  @Throws(Throwable::class)
  suspend fun revokeToken(token: String): Boolean {
    val response = ktorClient.post("${environment.baseUrl}/oauth/revoke") {
      contentType(ContentType.Application.Json)
      setBody(RevokeTokenInput(token))
    }
    return response.status == HttpStatusCode.OK
  }
}
