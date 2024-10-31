package com.hedvig.android.feature.login.genericauth

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.auth.event.AuthEvent
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.feature.login.otpinput.OtpInputViewModel
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OtpInformationViewModelTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun testNetworkError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())

    viewModel.viewState.test {
      skipItems(1)
      viewModel.submitCode("123456")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(true)
        assertThat(this.networkErrorMessage).isEqualTo(null)
      }

      authRepository.submitOtpResponse.add(SubmitOtpResult.Error("Error"))

      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(false)
        assertThat(this.networkErrorMessage).isEqualTo("Error")
      }
    }
  }

  @Test
  fun testDismissNetworkError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())
    val errorMessage = "Error"

    viewModel.viewState.test {
      skipItems(1)
      viewModel.submitCode("123456")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(true)
        assertThat(this.loadingResend).isEqualTo(false)
      }

      authRepository.submitOtpResponse.add(SubmitOtpResult.Error(errorMessage))
      assertThat(awaitItem().networkErrorMessage).isEqualTo(errorMessage)
      viewModel.dismissError()
      assertThat(awaitItem().networkErrorMessage).isEqualTo(null)
    }
  }

  @Test
  fun testOtpError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())

    viewModel.viewState.test {
      skipItems(1)
      viewModel.submitCode("123456")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(true)
        assertThat(this.loadingResend).isEqualTo(false)
      }

      authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
      assertThat(awaitItem().networkErrorMessage).isNotNull()
    }
  }

  @Test
  fun testOtpSuccess() = runTest {
    val authRepository = FakeAuthRepository()
    val authTokenService = TestAuthTokenService()
    val viewModel = testViewModel(authRepository, authTokenService)

    viewModel.viewState.test {
      skipItems(1)
      viewModel.submitCode("123456")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(true)
        assertThat(this.loadingResend).isEqualTo(false)
      }

      authRepository.submitOtpResponse.add(SubmitOtpResult.Success(AuthorizationCodeGrant("")))
      authRepository.exchangeResponse.add(AuthTokenResult.Success(accessToken, refreshToken))

      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(false)
        val authEvent = authTokenService.authEventTurbine.awaitItem()
        assertThat(authEvent).isInstanceOf<AuthEvent.LoggedIn>().run {
          prop(AuthEvent.LoggedIn::accessToken).isEqualTo(accessToken.token)
          prop(AuthEvent.LoggedIn::refreshToken).isEqualTo(refreshToken.token)
        }
        assertThat(this.navigateToLoginScreen).isTrue()
      }
    }
  }

  @Test
  fun testResendError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())
    val errorMessage = "Error"

    viewModel.viewState.test {
      skipItems(1)
      viewModel.resendCode()
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(false)
        assertThat(this.loadingResend).isEqualTo(true)
      }

      authRepository.resendOtpResponse.add(ResendOtpResult.Error(errorMessage))
      assertThat(awaitItem().networkErrorMessage).isEqualTo(errorMessage)
    }
  }

  @Test
  fun testResend() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())

    viewModel.viewState.test {
      skipItems(1)
      val viewStateContext = this
      viewModel.events.test {
        viewModel.resendCode()
        with(viewStateContext.awaitItem()) {
          assertThat(this.loadingCode).isEqualTo(false)
          assertThat(this.loadingResend).isEqualTo(true)
        }

        authRepository.resendOtpResponse.add(ResendOtpResult.Success)
        assertThat(viewStateContext.awaitItem().loadingResend).isEqualTo(false)
        val event = awaitItem()
        assertThat(event).isEqualTo(OtpInputViewModel.Event.CodeResent)
      }
    }
  }

  @Test
  fun testSetInput() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())
    viewModel.viewState.test {
      skipItems(1)
      viewModel.setInput("1")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(false)
        assertThat(this.loadingResend).isEqualTo(false)
        assertThat(this.input).isEqualTo("1")
      }

      viewModel.setInput("12")
      assertThat(awaitItem().input).isEqualTo("12")

      viewModel.setInput("123")
      assertThat(awaitItem().input).isEqualTo("123")
    }
  }

  @Test
  fun `updating the input after getting an error should clear the error`() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository, TestAuthTokenService())

    viewModel.viewState.test {
      skipItems(1)
      viewModel.submitCode("111111")
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(true)
        assertThat(this.loadingResend).isEqualTo(false)
        assertThat(this.networkErrorMessage).isNull()
      }

      authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
      with(awaitItem()) {
        assertThat(this.loadingCode).isEqualTo(false)
        assertThat(this.networkErrorMessage).isNotNull()
      }

      viewModel.setInput("1")
      assertThat(awaitItem().networkErrorMessage).isNull()
    }
  }

  private fun TestScope.testViewModel(
    authRepository: FakeAuthRepository,
    authTokenService: TestAuthTokenService,
  ): OtpInputViewModel {
    return OtpInputViewModel(
      verifyUrl = "verifytest",
      resendUrl = "resendtest",
      credential = "test@email.com",
      authTokenService = authTokenService,
      authRepository = authRepository,
      coroutineScope = backgroundScope,
    )
  }

  companion object {
    private val accessToken = AccessToken("testAccessToken", 100)
    private val refreshToken = RefreshToken("testRefreshToken", 100)
  }
}
