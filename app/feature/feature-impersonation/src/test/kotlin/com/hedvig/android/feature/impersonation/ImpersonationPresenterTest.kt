package com.hedvig.android.feature.impersonation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImpersonationPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val exchangeToken = "test-exchange-token"

  @Test
  fun `initial state shows loading`() = runTest {
    val presenter = createPresenter(
      exchangeResult = AuthTokenResult.Success(
        accessToken = AccessToken(token = "access", expiryInSeconds = 3600),
        refreshToken = RefreshToken(token = "refresh", expiryInSeconds = 7200),
      ),
    )

    presenter.test(ImpersonationUiState()) {
      val initialState = awaitItem()
      assertThat(initialState.isSuccess).isFalse()
      assertThat(initialState.errorMessage).isNull()
      assertThat(initialState.navigateToHome).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `successful exchange leads to success and navigation`() = runTest {
    var loginCalled = false
    val presenter = createPresenter(
      exchangeResult = AuthTokenResult.Success(
        accessToken = AccessToken(token = "access", expiryInSeconds = 3600),
        refreshToken = RefreshToken(token = "refresh", expiryInSeconds = 7200),
      ),
      onLogin = { _, _ -> loginCalled = true },
    )

    presenter.test(ImpersonationUiState()) {
      skipItems(1) // initial state

      // After exchange completes, should show success
      val successState = awaitItem()
      assertThat(successState.isSuccess).isTrue()
      assertThat(loginCalled).isTrue()

      // After delay, should navigate
      val navigateState = awaitItem()
      assertThat(navigateState.navigateToHome).isTrue()
    }
  }

  @Test
  fun `exchange error shows error message`() = runTest {
    val presenter = createPresenter(
      exchangeResult = AuthTokenResult.Error.BackendErrorResponse("Invalid token"),
    )

    presenter.test(ImpersonationUiState()) {
      skipItems(1) // initial state

      val errorState = awaitItem()
      assertThat(errorState.errorMessage).isNotNull()
      assertThat(errorState.isSuccess).isFalse()
      assertThat(errorState.navigateToHome).isFalse()
    }
  }

  @Test
  fun `state is preserved on back navigation`() = runTest {
    val preservedState = ImpersonationUiState(
      isSuccess = true,
      navigateToHome = false,
    )
    val presenter = createPresenter(
      exchangeResult = AuthTokenResult.Success(
        accessToken = AccessToken(token = "access", expiryInSeconds = 3600),
        refreshToken = RefreshToken(token = "refresh", expiryInSeconds = 7200),
      ),
    )

    presenter.test(preservedState) {
      val state = awaitItem()
      // State should be preserved without re-executing the exchange
      assertThat(state.isSuccess).isTrue()
      assertThat(state.navigateToHome).isFalse()
    }
  }

  private fun createPresenter(
    exchangeResult: AuthTokenResult,
    onLogin: suspend (AccessToken, RefreshToken) -> Unit = { _, _ -> },
  ) = ImpersonationPresenter(
    exchangeToken = exchangeToken,
    exchange = { exchangeResult },
    loginWithTokens = onLogin,
  )
}
