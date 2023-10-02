package com.hedvig.app.authenticate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.event.FakeAuthEventListener
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.hanalytics.test.FakeHAnalytics
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.StatusUrl
import com.hedvig.hanalytics.LoginMethod
import io.mockk.mockk
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BankIdLoginViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `start login attempt failing results in an error state immediately`() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authRepository)
    backgroundScope.launch { viewModel.viewState.collect() } // Start a subscriber since we're using WhileSubscribed

    authRepository.authAttemptResponse.add(AuthAttemptResult.Error("test error"))
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(
      BankIdLoginViewState.Error("Got Error when signing in with BankId: test error"),
    )
  }

  @Test
  fun `auth repository responding successfully to the exchange, results in a login`() = runTest {
    val authTokenService = testAuthTokenService()
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authRepository, authTokenService)
    backgroundScope.launch { viewModel.viewState.collect() }

    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    authRepository.authAttemptResponse.add(
      AuthAttemptResult.BankIdProperties("", StatusUrl(""), "autoStartToken"),
    )
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)

    authRepository.loginStatusResponse.add(LoginStatusResult.Pending("pending status message"))
    runCurrent()
    val pendingBankIdState = BankIdLoginViewState.HandlingBankId(
      "autoStartToken",
      false,
      LoginStatusResult.Pending("pending status message"),
      false,
    )
    assertThat(viewModel.viewState.value).isEqualTo(pendingBankIdState)
    authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
    runCurrent()
    // Before exchange resolves, still in pending state
    assertThat(viewModel.viewState.value).isEqualTo(pendingBankIdState)

    // Exchange succeeds
    authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(
      BankIdLoginViewState.HandlingBankId(
        "autoStartToken",
        false,
        LoginStatusResult.Completed(AuthorizationCodeGrant("grant")),
        false,
      ),
    )
    viewModel.didNavigateToLoginScreen()
    runCurrent()
    assertThat((viewModel.viewState.value as BankIdLoginViewState.HandlingBankId).processedNavigationToLoggedIn)
      .isTrue()
    val resultingTokens = authTokenService.getTokens()
    assertThat(resultingTokens).isNotNull()
    resultingTokens!!
    assertThat(resultingTokens.accessToken.token).isEqualTo("123")
    assertThat(resultingTokens.refreshToken.token).isEqualTo("456")
  }

  @Test
  fun `auth repository failing the exchange, results in an error`() = runTest {
    val authTokenService = testAuthTokenService()
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authRepository, authTokenService)
    backgroundScope.launch { viewModel.viewState.collect() }

    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    authRepository.authAttemptResponse.add(
      AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
    )
    authRepository.loginStatusResponse.add(LoginStatusResult.Pending("test"))
    runCurrent()
    assertThat((viewModel.viewState.value as BankIdLoginViewState.HandlingBankId).authStatus)
      .isEqualTo(LoginStatusResult.Pending("test"))
    authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
    // Exchange fails
    authRepository.exchangeResponse.add(AuthTokenResult.Error("failed"))
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Error("failed"))
    assertThat(authTokenService.getTokens()).isNull()
  }

  @Test
  fun `login status result failing, results in an error with the returned message`() = runTest {
    val authTokenService = testAuthTokenService()
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authRepository, authTokenService)
    backgroundScope.launch { viewModel.viewState.collect() }

    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    authRepository.authAttemptResponse.add(
      AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
    )
    authRepository.loginStatusResponse.add(LoginStatusResult.Pending("pending"))
    runCurrent()
    assertThat((viewModel.viewState.value as BankIdLoginViewState.HandlingBankId).authStatus)
      .isEqualTo(LoginStatusResult.Pending("pending"))
    authRepository.loginStatusResponse.add(LoginStatusResult.Failed("failed"))
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Error("failed"))
    assertThat(authTokenService.getTokens()).isNull()
  }

  @Test
  fun `login status result succeeding, sends a loggedIn event`() = runTest {
    val authEventListeners = List(2) { FakeAuthEventListener() }.toSet()
    val authEventBroadcaster = testAuthEventBroadcaster(authEventListeners)
    val authTokenService = testAuthTokenService(authEventBroadcaster)
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authRepository, authTokenService)
    backgroundScope.launch { viewModel.viewState.collect() }

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
  }

  private fun TestScope.testBankIdLoginViewModel(
    authRepository: AuthRepository,
    authTokenService: AuthTokenService = testAuthTokenService(),
  ): BankIdLoginViewModel {
    @Suppress("RemoveExplicitTypeArguments")
    return BankIdLoginViewModel(
      FakeHAnalytics(),
      FakeFeatureManager(loginMethod = { LoginMethod.BANK_ID_SWEDEN }),
      mockk<UploadMarketAndLanguagePreferencesUseCase>(relaxed = true),
      AuthTokenServiceProvider(
        object : DemoManager {
          override suspend fun isDemoMode(): Flow<Boolean> {
            return flowOf(false)
          }

          override suspend fun setDemoMode(demoMode: Boolean) {

          }
        },
        authTokenService,
        authTokenService,
      ),
      authRepository,
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
