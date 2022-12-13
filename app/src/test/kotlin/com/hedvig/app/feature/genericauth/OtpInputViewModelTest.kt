package com.hedvig.app.feature.genericauth

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.SubmitOtpResult
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OtpInputViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun testNetworkError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)

    viewModel.submitCode("123456")
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)

    authRepository.submitOtpResponse.add(SubmitOtpResult.Error("Error"))
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
  }

  @Test
  fun testDismissNetworkError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)
    val errorMessage = "Error"

    viewModel.submitCode("123456")
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(errorMessage))
    runCurrent()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(errorMessage)
    viewModel.dismissError()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
  }

  @Test
  fun testOtpError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)

    viewModel.submitCode("123456")
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
    runCurrent()
    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()
  }

  @Test
  fun testOtpSuccess() = runTest {
    val authRepository = FakeAuthRepository()
    val authTokenStorage = testAuthTokenStorage()
    val viewModel = testViewModel(authRepository, authTokenStorage)

    authTokenStorage.getTokens().first().also { (resultAccessToken, resultRefreshToken) ->
      assertThat(resultAccessToken?.token).isNull()
      assertThat(resultRefreshToken?.token).isNull()
    }
    viewModel.events.test {
      viewModel.submitCode("123456")
      runCurrent()
      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

      authRepository.submitOtpResponse.add(SubmitOtpResult.Success(AuthorizationCodeGrant("")))
      authRepository.exchangeResponse.add(AuthTokenResult.Success(accessToken, refreshToken))
      runCurrent()

      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
      authTokenStorage.getTokens().first().also { (resultAccessToken, resultRefreshToken) ->
        assertThat(resultAccessToken?.token).isEqualTo(accessToken.token)
        assertThat(resultRefreshToken?.token).isEqualTo(refreshToken.token)
      }
      val event = awaitItem()
      assertThat(event).isEqualTo(OtpInputViewModel.Event.Success(accessToken.token))
    }
  }

  @Test
  fun testResendError() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)
    val errorMessage = "Error"

    viewModel.resendCode()
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

    authRepository.resendOtpResponse.add(ResendOtpResult.Error(errorMessage))
    runCurrent()
    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(errorMessage)
  }

  @Test
  fun testResend() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)

    viewModel.events.test {
      viewModel.resendCode()
      runCurrent()
      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

      authRepository.resendOtpResponse.add(ResendOtpResult.Success)
      runCurrent()
      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
      val event = awaitItem()
      assertThat(event).isEqualTo(OtpInputViewModel.Event.CodeResent)
    }
  }

  @Test
  fun testSetInput() = runTest {
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)
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
    val authRepository = FakeAuthRepository()
    val viewModel = testViewModel(authRepository)

    viewModel.submitCode("111111")
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()

    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
    runCurrent()
    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()

    viewModel.setInput("1")
    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()
  }

  private fun TestScope.testViewModel(
    authRepository: FakeAuthRepository,
    authTokenStorage: AuthTokenStorage = testAuthTokenStorage(),
  ): OtpInputViewModel {
    return OtpInputViewModel(
      verifyUrl = "verifytest",
      resendUrl = "resendtest",
      credential = "test@email.com",
      authTokenService = testAuthTokenService(authRepository, authTokenStorage),
      authRepository = authRepository,
      uploadMarketAndLanguagePreferencesUseCase = mockk(relaxed = true),
    )
  }

  private fun TestScope.testAuthTokenStorage(): AuthTokenStorage {
    return AuthTokenStorage(
      dataStore = TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        backgroundScope,
      ),
    )
  }

  private fun TestScope.testAuthTokenService(
    authRepository: AuthRepository,
    authTokenStorage: AuthTokenStorage,
  ): AuthTokenService {
    return AuthTokenServiceImpl(
      authTokenStorage = authTokenStorage,
      authRepository = authRepository,
      coroutineScope = backgroundScope,
    )
  }

  companion object {
    private val accessToken = AccessToken("testAccessToken", 100)
    private val refreshToken = RefreshToken("testRefreshToken", 100)
  }
}
