package com.hedvig.app.authenticate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.tracking.MockHAnalytics
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.coroutines.MainCoroutineRule
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
import kotlinx.coroutines.flow.collect
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

  @Test
  fun `start login attempt failing results in an error state immediately`() = runTest {
    val authTokenService = testAuthTokenService()
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authTokenService, authRepository)
    backgroundScope.launch { viewModel.viewState.collect() } // Start a subscriber since we're using WhileSubscribed

    authRepository.authAttemptResponse.add(AuthAttemptResult.Error(""))
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Error(null))
  }

  @Test
  fun `auth repository responding successfully to the exchange, results in a login`() = runTest {
    val authTokenService = testAuthTokenService()
    val authRepository = FakeAuthRepository()
    val viewModel: BankIdLoginViewModel = testBankIdLoginViewModel(authTokenService, authRepository)
    backgroundScope.launch { viewModel.viewState.collect() }

    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)
    authRepository.authAttemptResponse.add(
      AuthAttemptResult.BankIdProperties("", StatusUrl(""), "autoStartToken"),
    )
    runCurrent()
    assertThat(viewModel.viewState.value).isEqualTo(BankIdLoginViewState.Loading)

    authRepository.loginStatusResponse.add(LoginStatusResult.Pending(null))
    runCurrent()
    val pendingBankIdState = BankIdLoginViewState.HandlingBankId(
      "autoStartToken",
      false,
      LoginStatusResult.Pending(null),
      false,
    )
    assertThat(viewModel.viewState.value).isEqualTo(pendingBankIdState)
    authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
    runCurrent()
    // Before exchange resolves, still in pending state
    assertThat(viewModel.viewState.value).isEqualTo(pendingBankIdState)

    // When exchange succeeds
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

  private fun TestScope.testBankIdLoginViewModel(
    authTokenService: AuthTokenService = testAuthTokenService(),
    authRepository: AuthRepository = FakeAuthRepository(),
  ): BankIdLoginViewModel {
    @Suppress("RemoveExplicitTypeArguments")
    return BankIdLoginViewModel(
      MockHAnalytics(),
      FakeFeatureManager(loginMethod = { LoginMethod.BANK_ID_SWEDEN }),
      mockk<PushTokenManager>(relaxed = true),
      mockk<UploadMarketAndLanguagePreferencesUseCase>(relaxed = true),
      authTokenService,
      authRepository,
    )
  }

  private fun TestScope.testAuthTokenService(): AuthTokenService {
    return AuthTokenServiceImpl(
      AuthTokenStorage(
        dataStore = TestPreferencesDataStore(
          datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
          coroutineScope = backgroundScope,
        ),
      ),
      FakeAuthRepository(),
      backgroundScope,
    )
  }
}
