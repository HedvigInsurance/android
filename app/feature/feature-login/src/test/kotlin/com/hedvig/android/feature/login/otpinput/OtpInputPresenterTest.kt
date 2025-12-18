package com.hedvig.android.feature.login.otpinput

import app.cash.turbine.Turbine
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.SubmitOtpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test

class OtpInputPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val testCredential = "test@email.com"
  private val testAccessToken = AccessToken("testAccessToken", 100)
  private val testRefreshToken = RefreshToken("testRefreshToken", 100)

  private fun createPresenter(
    submitOtpResponse: Turbine<SubmitOtpResult>,
    resendOtpResponse: Turbine<ResendOtpResult> = Turbine(),
    exchangeResponse: Turbine<AuthTokenResult> = Turbine(),
    authStatus: MutableStateFlow<AuthStatus?> = MutableStateFlow(null),
    loginWithTokensCalled: MutableList<Pair<AccessToken, RefreshToken>> = mutableListOf(),
  ) = OtpInputPresenter(
    verifyUrl = "verifyUrl",
    resendUrl = "resendUrl",
    credential = testCredential,
    submitOtp = { _, _ -> submitOtpResponse.awaitItem() },
    resendOtp = { _ -> resendOtpResponse.awaitItem() },
    exchange = { _ -> exchangeResponse.awaitItem() },
    loginWithTokens = { accessToken, refreshToken ->
      loginWithTokensCalled.add(accessToken to refreshToken)
      val futureExpiry = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
      authStatus.value = AuthStatus.LoggedIn(
        accessToken = LocalAccessToken(accessToken.token, futureExpiry),
        refreshToken = LocalRefreshToken(refreshToken.token, futureExpiry),
      )
    },
    authStatus = authStatus,
  )

  @Test
  fun `initial state has empty input and correct credential`() = runTest {
    val presenter = createPresenter(Turbine())

    presenter.test(OtpInputUiState(credential = testCredential)) {
      val state = awaitItem()
      assertThat(state.input).isEqualTo("")
      assertThat(state.credential).isEqualTo(testCredential)
      assertThat(state.loadingCode).isFalse()
      assertThat(state.loadingResend).isFalse()
      assertThat(state.networkErrorMessage).isNull()
      assertThat(state.navigateToLoginScreen).isFalse()
    }
  }

  @Test
  fun `set input updates state and clears error`() = runTest {
    val presenter = createPresenter(Turbine())
    val initialState = OtpInputUiState(
      credential = testCredential,
      networkErrorMessage = "Some error",
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(OtpInputEvent.SetInput("123"))

      val state = awaitItem()
      assertThat(state.input).isEqualTo("123")
      assertThat(state.networkErrorMessage).isNull()
    }
  }

  @Test
  fun `submit code shows loading state`() = runTest {
    val submitOtpResponse = Turbine<SubmitOtpResult>()
    val presenter = createPresenter(submitOtpResponse)

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.SubmitCode("123456"))

      val loadingState = awaitItem()
      assertThat(loadingState.loadingCode).isTrue()
      assertThat(loadingState.networkErrorMessage).isNull()

      submitOtpResponse.add(SubmitOtpResult.Error("Error"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submit code with error shows error message`() = runTest {
    val submitOtpResponse = Turbine<SubmitOtpResult>()
    val presenter = createPresenter(submitOtpResponse)

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.SubmitCode("123456"))
      awaitItem() // Loading state

      submitOtpResponse.add(SubmitOtpResult.Error("Error message"))

      val errorState = awaitItem()
      assertThat(errorState.loadingCode).isFalse()
      assertThat(errorState.networkErrorMessage).isEqualTo("Error message")
    }
  }

  @Test
  fun `submit code success then exchange success logs in`() = runTest {
    val submitOtpResponse = Turbine<SubmitOtpResult>()
    val exchangeResponse = Turbine<AuthTokenResult>()
    val authStatus = MutableStateFlow<AuthStatus?>(null)
    val loginCalls = mutableListOf<Pair<AccessToken, RefreshToken>>()
    val presenter = createPresenter(
      submitOtpResponse = submitOtpResponse,
      exchangeResponse = exchangeResponse,
      authStatus = authStatus,
      loginWithTokensCalled = loginCalls,
    )

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.SubmitCode("123456"))
      awaitItem() // Loading state

      submitOtpResponse.add(SubmitOtpResult.Success(AuthorizationCodeGrant("authCode")))
      exchangeResponse.add(AuthTokenResult.Success(testAccessToken, testRefreshToken))

      val successState = awaitItem()
      assertThat(successState.loadingCode).isFalse()
      assertThat(loginCalls).isEqualTo(listOf(testAccessToken to testRefreshToken))

      // Auth status triggers navigation
      val navigateState = awaitItem()
      assertThat(navigateState.navigateToLoginScreen).isTrue()
    }
  }

  @Test
  fun `submit code success then exchange error shows error`() = runTest {
    val submitOtpResponse = Turbine<SubmitOtpResult>()
    val exchangeResponse = Turbine<AuthTokenResult>()
    val presenter = createPresenter(
      submitOtpResponse = submitOtpResponse,
      exchangeResponse = exchangeResponse,
    )

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.SubmitCode("123456"))
      awaitItem() // Loading state

      submitOtpResponse.add(SubmitOtpResult.Success(AuthorizationCodeGrant("authCode")))
      exchangeResponse.add(AuthTokenResult.Error.BackendErrorResponse("Backend error"))

      val errorState = awaitItem()
      assertThat(errorState.loadingCode).isFalse()
      assertThat(errorState.networkErrorMessage).isEqualTo("Error:Backend error")
    }
  }

  @Test
  fun `resend code shows loading state`() = runTest {
    val resendOtpResponse = Turbine<ResendOtpResult>()
    val presenter = createPresenter(
      submitOtpResponse = Turbine(),
      resendOtpResponse = resendOtpResponse,
    )

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.ResendCode)

      val loadingState = awaitItem()
      assertThat(loadingState.loadingResend).isTrue()

      resendOtpResponse.add(ResendOtpResult.Success)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `resend code success sets event and clears input`() = runTest {
    val resendOtpResponse = Turbine<ResendOtpResult>()
    val presenter = createPresenter(
      submitOtpResponse = Turbine(),
      resendOtpResponse = resendOtpResponse,
    )
    val initialState = OtpInputUiState(
      credential = testCredential,
      input = "123456",
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(OtpInputEvent.ResendCode)
      awaitItem() // Loading state

      resendOtpResponse.add(ResendOtpResult.Success)

      val successState = awaitItem()
      assertThat(successState.loadingResend).isFalse()
      assertThat(successState.input).isEqualTo("")
      assertThat(successState.codeResentEvent).isTrue()
    }
  }

  @Test
  fun `resend code error shows error message`() = runTest {
    val resendOtpResponse = Turbine<ResendOtpResult>()
    val presenter = createPresenter(
      submitOtpResponse = Turbine(),
      resendOtpResponse = resendOtpResponse,
    )

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.ResendCode)
      awaitItem() // Loading state

      resendOtpResponse.add(ResendOtpResult.Error("Resend failed"))

      val errorState = awaitItem()
      assertThat(errorState.loadingResend).isFalse()
      assertThat(errorState.networkErrorMessage).isEqualTo("Resend failed")
    }
  }

  @Test
  fun `dismiss error clears error message`() = runTest {
    val presenter = createPresenter(Turbine())
    val initialState = OtpInputUiState(
      credential = testCredential,
      networkErrorMessage = "Some error",
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(OtpInputEvent.DismissError)

      val state = awaitItem()
      assertThat(state.networkErrorMessage).isNull()
    }
  }

  @Test
  fun `handled code resent event clears the event flag`() = runTest {
    val presenter = createPresenter(Turbine())
    val initialState = OtpInputUiState(
      credential = testCredential,
      codeResentEvent = true,
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(OtpInputEvent.HandledCodeResentEvent)

      val state = awaitItem()
      assertThat(state.codeResentEvent).isFalse()
    }
  }

  @Test
  fun `state is preserved on back navigation`() = runTest {
    val presenter = createPresenter(Turbine())
    val preservedState = OtpInputUiState(
      credential = testCredential,
      input = "123",
      loadingCode = false,
    )

    presenter.test(preservedState) {
      val state = awaitItem()
      assertThat(state.input).isEqualTo("123")
      assertThat(state.credential).isEqualTo(testCredential)
    }
  }

  @Test
  fun `multiple input changes update state correctly`() = runTest {
    val presenter = createPresenter(Turbine())

    presenter.test(OtpInputUiState(credential = testCredential)) {
      awaitItem()

      sendEvent(OtpInputEvent.SetInput("1"))
      assertThat(awaitItem().input).isEqualTo("1")

      sendEvent(OtpInputEvent.SetInput("12"))
      assertThat(awaitItem().input).isEqualTo("12")

      sendEvent(OtpInputEvent.SetInput("123"))
      assertThat(awaitItem().input).isEqualTo("123")
    }
  }

  @Test
  fun `submit while loading is ignored`() = runTest {
    val presenter = createPresenter(Turbine())
    val loadingState = OtpInputUiState(
      credential = testCredential,
      loadingCode = true,
    )

    presenter.test(loadingState) {
      awaitItem()

      sendEvent(OtpInputEvent.SubmitCode("123456"))

      expectNoEvents()
    }
  }

  @Test
  fun `resend while loading is ignored`() = runTest {
    val presenter = createPresenter(Turbine())
    val loadingState = OtpInputUiState(
      credential = testCredential,
      loadingResend = true,
    )

    presenter.test(loadingState) {
      awaitItem()

      sendEvent(OtpInputEvent.ResendCode)

      expectNoEvents()
    }
  }

  @Test
  fun `input change after error clears error`() = runTest {
    val presenter = createPresenter(Turbine())
    val initialState = OtpInputUiState(
      credential = testCredential,
      networkErrorMessage = "Error",
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(OtpInputEvent.SetInput("1"))

      val state = awaitItem()
      assertThat(state.networkErrorMessage).isNull()
      assertThat(state.input).isEqualTo("1")
    }
  }
}
