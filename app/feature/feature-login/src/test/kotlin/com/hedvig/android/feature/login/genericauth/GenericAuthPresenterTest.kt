package com.hedvig.android.feature.login.genericauth

import app.cash.turbine.Turbine
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.StatusUrl
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GenericAuthPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private fun createPresenter(startLoginAttemptResponse: Turbine<AuthAttemptResult>) = GenericAuthPresenter(
    startLoginAttempt = { _ -> startLoginAttemptResponse.awaitItem() },
  )

  private fun createPresenterWithImmediateResponse(
    startLoginAttempt: suspend (email: String) -> AuthAttemptResult = { _ ->
      AuthAttemptResult.OtpProperties(
        id = "123",
        statusUrl = StatusUrl("testStatusUrl"),
        resendUrl = "resendUrl",
        verifyUrl = "verifyUrl",
        maskedEmail = null,
      )
    },
  ) = GenericAuthPresenter(
    startLoginAttempt = startLoginAttempt,
  )

  @Test
  fun `initial state has empty email input`() = runTest {
    val presenter = createPresenterWithImmediateResponse()

    presenter.test(GenericAuthViewState()) {
      val initialState = awaitItem()
      assertThat(initialState.emailInput).isEqualTo("")
      assertThat(initialState.error).isNull()
      assertThat(initialState.loading).isFalse()
      assertThat(initialState.verifyUrl).isNull()
      assertThat(initialState.resendUrl).isNull()
    }
  }

  @Test
  fun `set email input updates state and clears error`() = runTest {
    val presenter = createPresenterWithImmediateResponse()
    val initialState = GenericAuthViewState(
      emailInput = "",
      error = GenericAuthViewState.TextFieldError.Other.InvalidEmail,
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("test@email.com"))

      val state = awaitItem()
      assertThat(state.emailInput).isEqualTo("test@email.com")
      assertThat(state.error).isNull()
    }
  }

  @Test
  fun `submit invalid email shows invalid email error`() = runTest {
    val presenter = createPresenterWithImmediateResponse()

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("invalid email"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      val state = awaitItem()
      assertThat(state.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.InvalidEmail)
      assertThat(state.loading).isFalse()
    }
  }

  @Test
  fun `submit empty email shows empty error`() = runTest {
    val presenter = createPresenterWithImmediateResponse()

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      val state = awaitItem()
      assertThat(state.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.Empty)
      assertThat(state.loading).isFalse()
    }
  }

  @Test
  fun `submit valid email shows loading state`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("valid@email.com"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      val loadingState = awaitItem()
      assertThat(loadingState.loading).isTrue()

      responseTurbine.add(
        AuthAttemptResult.OtpProperties(
          id = "123",
          statusUrl = StatusUrl("statusUrl"),
          resendUrl = "testResendUrl",
          verifyUrl = "testVerifyUrl",
          maskedEmail = null,
        ),
      )

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submit valid email successfully sets verify and resend urls`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("valid@email.com"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Loading state
      awaitItem()

      responseTurbine.add(
        AuthAttemptResult.OtpProperties(
          id = "123",
          statusUrl = StatusUrl("statusUrl"),
          resendUrl = "testResendUrl",
          verifyUrl = "testVerifyUrl",
          maskedEmail = null,
        ),
      )

      val successState = awaitItem()
      assertThat(successState.loading).isFalse()
      assertThat(successState.verifyUrl).isEqualTo("testVerifyUrl")
      assertThat(successState.resendUrl).isEqualTo("testResendUrl")
      assertThat(successState.error).isNull()
    }
  }

  @Test
  fun `submit valid email with network error shows error`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("valid@email.com"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Loading state
      awaitItem()

      responseTurbine.add(AuthAttemptResult.Error.IOError("Network error"))

      val errorState = awaitItem()
      assertThat(errorState.loading).isFalse()
      assertThat(errorState.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.NetworkError)
      assertThat(errorState.verifyUrl).isNull()
    }
  }

  @Test
  fun `submit valid email with localized error shows message`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("valid@email.com"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Loading state
      awaitItem()

      responseTurbine.add(AuthAttemptResult.Error.Localised("Custom error message"))

      val errorState = awaitItem()
      assertThat(errorState.error).isEqualTo(GenericAuthViewState.TextFieldError.Message("Custom error message"))
    }
  }

  @Test
  fun `submit valid email with bank id result shows network error`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("valid@email.com"))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Loading state
      awaitItem()

      responseTurbine.add(
        AuthAttemptResult.BankIdProperties(
          id = "123",
          statusUrl = StatusUrl("statusUrl"),
          autoStartToken = "token",
        ),
      )

      val errorState = awaitItem()
      assertThat(errorState.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.NetworkError)
    }
  }

  @Test
  fun `onStartOtpInput clears verify url`() = runTest {
    val presenter = createPresenterWithImmediateResponse()
    val initialState = GenericAuthViewState(
      verifyUrl = "someUrl",
      resendUrl = "someResendUrl",
    )

    presenter.test(initialState) {
      awaitItem()

      sendEvent(GenericAuthEvent.OnStartOtpInput)

      val state = awaitItem()
      assertThat(state.verifyUrl).isNull()
      assertThat(state.resendUrl).isEqualTo("someResendUrl")
    }
  }

  @Test
  fun `email with whitespace is trimmed and validated`() = runTest {
    val responseTurbine = Turbine<AuthAttemptResult>()
    val presenter = createPresenter(responseTurbine)

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput(" valid@email.com "))
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Loading state
      awaitItem()

      responseTurbine.add(
        AuthAttemptResult.OtpProperties(
          id = "123",
          statusUrl = StatusUrl("statusUrl"),
          resendUrl = "resendUrl",
          verifyUrl = "verifyUrl",
          maskedEmail = null,
        ),
      )

      val successState = awaitItem()
      assertThat(successState.loading).isFalse()
      assertThat(successState.verifyUrl).isNotNull()
      assertThat(successState.error).isNull()
    }
  }

  @Test
  fun `state is preserved on back navigation`() = runTest {
    val presenter = createPresenterWithImmediateResponse()
    val preservedState = GenericAuthViewState(
      emailInput = "preserved@email.com",
      verifyUrl = "preservedUrl",
      resendUrl = "preservedResendUrl",
    )

    presenter.test(preservedState) {
      val state = awaitItem()
      assertThat(state.emailInput).isEqualTo("preserved@email.com")
      assertThat(state.verifyUrl).isEqualTo("preservedUrl")
      assertThat(state.resendUrl).isEqualTo("preservedResendUrl")
    }
  }

  @Test
  fun `multiple email input changes update state correctly`() = runTest {
    val presenter = createPresenterWithImmediateResponse()

    presenter.test(GenericAuthViewState()) {
      awaitItem()

      sendEvent(GenericAuthEvent.SetEmailInput("test1"))
      val state1 = awaitItem()
      assertThat(state1.emailInput).isEqualTo("test1")

      sendEvent(GenericAuthEvent.SetEmailInput("test2@email.com"))
      val state2 = awaitItem()
      assertThat(state2.emailInput).isEqualTo("test2@email.com")

      sendEvent(GenericAuthEvent.SetEmailInput(""))
      val state3 = awaitItem()
      assertThat(state3.emailInput).isEqualTo("")
    }
  }

  @Test
  fun `submit while loading is ignored`() = runTest {
    val presenter = createPresenterWithImmediateResponse()
    val loadingState = GenericAuthViewState(
      emailInput = "valid@email.com",
      loading = true,
    )

    presenter.test(loadingState) {
      awaitItem()

      sendEvent(GenericAuthEvent.SubmitEmail)

      // Should not emit new state since already loading
      expectNoEvents()
    }
  }
}
