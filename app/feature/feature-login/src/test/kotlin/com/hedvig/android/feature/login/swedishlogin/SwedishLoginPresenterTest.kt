package com.hedvig.android.feature.login.swedishlogin

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.event.FakeAuthEventListener
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.TestPreferencesDataStore
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.coroutines.EmptyCoroutineContext

class SwedishLoginPresenterTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `start login attempt failing results in an error state immediately`() = runTest {
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository)

    presenter.test(SwedishLoginUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.Loading>()
      authRepository.authAttemptResponse.add(AuthAttemptResult.Error("test error"))
      assertThat(awaitItem()).isInstanceOf<SwedishLoginUiState.StartLoginAttemptFailed>()
    }
  }

  @Test
  fun `auth repository responding successfully to the exchange, results in a login`() = runTest {
    val authTokenService = testAuthTokenService()
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
      val resultingTokens = authTokenService.getTokens()
      assertThat(resultingTokens).isNotNull().apply {
        prop(AuthTokens::accessToken).prop(LocalAccessToken::token).isEqualTo("123")
        prop(AuthTokens::refreshToken).prop(LocalRefreshToken::token).isEqualTo("456")
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
    val authTokenService = testAuthTokenService()
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
      authRepository.exchangeResponse.add(AuthTokenResult.Error("failed"))
      assertThat(awaitItem()).isEqualTo(SwedishLoginUiState.BankIdError("failed"))
      assertThat(authTokenService.getTokens()).isNull()
    }
  }

  @Test
  fun `login status result failing, results in an error with the returned message`() = runTest {
    val authTokenService = testAuthTokenService()
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
      assertThat(authTokenService.getTokens()).isNull()
    }
  }

  @Test
  fun `login status result succeeding, sends a loggedIn event`() = runTest {
    val authEventListeners = List(2) { FakeAuthEventListener() }.toSet()
    val authEventBroadcaster = testAuthEventBroadcaster(authEventListeners)
    val authTokenService = testAuthTokenService(authEventBroadcaster)
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState.Loading) {
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
      for (authEventListener in authEventListeners) {
        val accessToken = authEventListener.loggedInEvent.awaitItem()
        assertThat(accessToken).isEqualTo("123")
        authEventListener.loggedInEvent.expectNoEvents()
        authEventListener.loggedOutEvent.expectNoEvents()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.testSwedishLoginPresenter(
    authRepository: AuthRepository,
    authTokenService: AuthTokenService = testAuthTokenService(),
  ): SwedishLoginPresenter {
    @Suppress("RemoveExplicitTypeArguments")
    return SwedishLoginPresenter(
      authTokenService,
      authRepository,
      object : DemoManager {
        override suspend fun isDemoMode(): Flow<Boolean> = flowOf(false)
        override suspend fun setDemoMode(demoMode: Boolean) {}
      },
    )
  }

  private fun TestScope.testAuthEventBroadcaster(
    authEventListeners: Set<AuthEventListener> = emptySet(),
  ): AuthEventBroadcaster {
    return AuthEventBroadcaster(
      authEventListeners,
      ApplicationScope(backgroundScope),
      EmptyCoroutineContext,
    )
  }

  private fun TestScope.testAuthTokenService(
    authEventBroadcaster: AuthEventBroadcaster = testAuthEventBroadcaster(),
  ): AuthTokenService {
    return AuthTokenServiceImpl(
      AuthTokenStorage(
        dataStore = TestPreferencesDataStore(
          datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
          coroutineScope = backgroundScope,
        ),
      ),
      FakeAuthRepository(),
      authEventBroadcaster,
      backgroundScope,
    )
  }
}
