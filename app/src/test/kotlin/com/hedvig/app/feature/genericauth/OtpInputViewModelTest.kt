package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.Grant
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.RevokeResult
import com.hedvig.authlib.StatusUrl
import com.hedvig.authlib.SubmitOtpResult
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OtpInputViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private var authToken: String? = "testToken"

  private var otpResult: SubmitOtpResult = SubmitOtpResult.Success(AuthorizationCodeGrant("test"))
  private var resendOtpResult: ResendOtpResult = ResendOtpResult.Success

  private val viewModel = OtpInputViewModel(
    verifyUrl = "verifytest",
    resendUrl = "resendtest",
    credential = "test@email.com",
    authenticationTokenService = object : AuthenticationTokenService {
      override var authenticationToken: String?
        get() = authToken
        set(value) {
          authToken = value
        }
      override var refreshToken: RefreshToken? = null
    },
    authRepository = object : AuthRepository {
      override suspend fun startLoginAttempt(
        loginMethod: LoginMethod,
        market: String,
        personalNumber: String?,
        email: String?,
      ): AuthAttemptResult {
        TODO("Not yet implemented")
      }

      override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
        TODO("Not yet implemented")
      }

      override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
        delay(100.milliseconds)
        return otpResult
      }

      override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
        delay(100.milliseconds)
        return resendOtpResult
      }

      override suspend fun exchange(grant: Grant): AuthTokenResult {
        delay(100.milliseconds)
        return AuthTokenResult.Success(
          accessToken = AccessToken(authToken!!, 100),
          refreshToken = RefreshToken("test", 100),
        )
      }

      override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
        TODO("Not yet implemented")
      }

      override suspend fun revoke(token: String): RevokeResult {
        TODO("Not yet implemented")
      }
    },
    uploadMarketAndLanguagePreferencesUseCase = mockk(relaxed = true),
  )

  @Test
  fun testNetworkError() = runTest {
    otpResult = SubmitOtpResult.Error("Error")

    viewModel.submitCode("123456")

    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
  }

  @Test
  fun testDismissNetworkError() = runTest {
    otpResult = SubmitOtpResult.Error("Error")

    viewModel.submitCode("123456")
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
    viewModel.dismissError()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
  }

  @Test
  fun testOtpError() = runTest {
    otpResult = SubmitOtpResult.Error("Error")

    viewModel.submitCode("123456")
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()
  }

  @Test
  fun testOtpSuccess() = runTest {
    otpResult = SubmitOtpResult.Success(AuthorizationCodeGrant("testOtpSuccess"))

    val events = mutableListOf<OtpInputViewModel.Event>()
    val eventCollectingJob = launch {
      viewModel.events.collect { events.add(it) }
    }

    viewModel.submitCode("123456")
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(authToken).isEqualTo("testToken")
    assertThat(events.size).isEqualTo(1)
    assertThat(events.first()).isEqualTo(OtpInputViewModel.Event.Success("testToken"))
    eventCollectingJob.cancel()
  }

  @Test
  fun testResendError() = runTest {
    resendOtpResult = ResendOtpResult.Error("Error")

    viewModel.resendCode()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
  }

  @Test
  fun testResend() = runTest {
    resendOtpResult = ResendOtpResult.Success

    val events = mutableListOf<OtpInputViewModel.Event>()
    val eventCollectingJob = launch {
      viewModel.events.collect { events.add(it) }
    }

    viewModel.resendCode()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
    assertThat(events.size).isEqualTo(1)
    assertThat(events.first()).isEqualTo(OtpInputViewModel.Event.CodeResent)
    eventCollectingJob.cancel()
  }

  @Test
  fun testSetInput() = runTest {
    viewModel.setInput("1")
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
    assertThat(viewModel.viewState.value.input).isEqualTo("1")

    viewModel.setInput("12")
    assertThat(viewModel.viewState.value.input).isEqualTo("12")

    viewModel.setInput("123")
    assertThat(viewModel.viewState.value.input).isEqualTo("123")
  }

  @Test
  fun `updating the input after getting an error should clear the error`() = runTest {
    otpResult = SubmitOtpResult.Error("Error")
    viewModel.setInput("111111")
    viewModel.submitCode("111111")
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()

    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()

    viewModel.setInput("1")
    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()
  }
}
