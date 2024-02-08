package com.hedvig.android.feature.login.swedishlogin

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.event.AuthEvent
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.StatusUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SwedishLoginPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `start login attempt failing results in an error state immediately`() = runTest {
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository)

    presenter.test(SwedishLoginUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.Loading>()
      authRepository.authAttemptResponse.add(AuthAttemptResult.Error.UnknownError("test error"))
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.StartLoginAttemptFailed>()
    }
  }

  @Test
  fun `auth repository responding successfully to the exchange, results in a login`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.Loading>()
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), "autoStartToken"),
      )
      awaitUnchanged()

      authRepository.loginStatusResponse.add(LoginStatusResult.Pending("pending status message"))
      assertThat(awaitItem()).isEqualTo(
        SwedishLoginUiState.HandlingBankId(
          autoStartToken = SwedishLoginUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          loginStatusResult = LoginStatusResult.Pending("pending status message"),
          allowOpeningBankId = true,
          navigateToLoginScreen = false,
        ),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      // Before exchange resolves, still in pending state
      expectNoEvents()
      sendEvent(SwedishLoginEvent.DidOpenBankIDApp)
      assertThat(awaitItem()).isEqualTo(
        SwedishLoginUiState.HandlingBankId(
          SwedishLoginUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          LoginStatusResult.Pending("pending status message"),
          false,
          false,
        ),
      )

      // Exchange succeeds
      authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
      assertThat(awaitItem()).isEqualTo(
        SwedishLoginUiState.HandlingBankId(
          SwedishLoginUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          LoginStatusResult.Completed(AuthorizationCodeGrant("grant")),
          false,
          true,
        ),
      )
      val resultingTokens: AuthEvent = authTokenService.authEventTurbine.awaitItem()
      assertThat(resultingTokens).isInstanceOf<AuthEvent.LoggedIn>().apply {
        prop(AuthEvent.LoggedIn::accessToken).isEqualTo("123")
        prop(AuthEvent.LoggedIn::refreshToken).isEqualTo("456")
      }

      sendEvent(SwedishLoginEvent.DidNavigateToLoginScreen)
      assertThat(awaitItem()).isEqualTo(
        SwedishLoginUiState.HandlingBankId(
          SwedishLoginUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          LoginStatusResult.Completed(AuthorizationCodeGrant("grant")),
          false,
          false,
        ),
      )
    }
  }

  @Test
  fun `auth repository failing the exchange, results in an error`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.Loading>()
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Pending("test"))
      assertThat((awaitItem() as SwedishLoginUiState.HandlingBankId).loginStatusResult)
        .isEqualTo(LoginStatusResult.Pending("test"))
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      expectNoEvents()
      // Exchange fails
      authRepository.exchangeResponse.add(AuthTokenResult.Error.UnknownError("failed"))
      assertThat(awaitItem()).isEqualTo(SwedishLoginUiState.BankIdError("failed"))
      authTokenService.authEventTurbine.expectNoEvents()
    }
  }

  @Test
  fun `login status result failing, results in an error with the returned message`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(SwedishLoginUiState.Loading)
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Pending("pending"))
      assertThat((awaitItem() as SwedishLoginUiState.HandlingBankId).loginStatusResult)
        .isEqualTo(LoginStatusResult.Pending("pending"))
      authRepository.loginStatusResponse.add(LoginStatusResult.Failed("failed"))
      assertThat(awaitItem()).isEqualTo(SwedishLoginUiState.BankIdError("failed"))
      authTokenService.authEventTurbine.expectNoEvents()
    }
  }

  @Test
  fun `login status result succeeding, sends a loggedIn event`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState.Loading) {
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
      val accessToken = (authTokenService.authEventTurbine.awaitItem() as AuthEvent.LoggedIn).accessToken
      assertThat(accessToken).isEqualTo("123")
      authTokenService.authEventTurbine.expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun testSwedishLoginPresenter(
    authRepository: AuthRepository,
    authTokenService: AuthTokenService = TestAuthTokenService(),
  ): SwedishLoginPresenter {
    return SwedishLoginPresenter(
      authTokenService,
      authRepository,
      object : DemoManager {
        override fun isDemoMode(): Flow<Boolean> = flowOf(false)

        override suspend fun setDemoMode(demoMode: Boolean) {}
      },
    )
  }
}
