package com.hedvig.android.feature.login.swedishlogin

import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwedishLoginPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `start login attempt failing results in an error state immediately`() = runTest {
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository)

    presenter.test(SwedishLoginUiState(BankIdUiState.Loading, false)) {
      assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
      authRepository.authAttemptResponse.add(AuthAttemptResult.Error.UnknownError("test error"))
      assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.StartLoginAttemptFailed>()
    }
  }

  @Test
  fun `auth repository responding successfully to the exchange, results in a login`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState(BankIdUiState.Loading, false)) {
      assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), "autoStartToken"),
      )
      awaitUnchanged()

      authRepository.loginStatusResponse.add(LoginStatusResult.Pending("pending status message", null))
      assertThat(awaitItem().bankIdUiState).isEqualTo(
        BankIdUiState.HandlingBankId(
          statusMessage = "pending status message",
          autoStartToken = BankIdUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          bankIdLiveQrCodeData = null,
          bankIdAppOpened = false,
          allowOpeningBankId = true,
        ),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      // Before exchange resolves, still in pending state
      expectNoEvents()
      sendEvent(SwedishLoginEvent.DidOpenBankIDApp)
      assertThat(awaitItem().bankIdUiState).isEqualTo(
        BankIdUiState.HandlingBankId(
          statusMessage = "pending status message",
          autoStartToken = BankIdUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          bankIdLiveQrCodeData = null,
          bankIdAppOpened = false,
          allowOpeningBankId = false,
        ),
      )

      // Exchange succeeds
      authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
      val item = awaitItem()
      assertThat(item.navigateToLoginScreen).isTrue()
      assertThat(item.bankIdUiState).isEqualTo(
        BankIdUiState.HandlingBankId(
          statusMessage = "pending status message",
          autoStartToken = BankIdUiState.HandlingBankId.AutoStartToken("autoStartToken"),
          bankIdLiveQrCodeData = null,
          bankIdAppOpened = false,
          allowOpeningBankId = false,
        ),
      )
      val resultingTokens: AuthEvent = authTokenService.authEventTurbine.awaitItem()
      assertThat(resultingTokens).isInstanceOf<AuthEvent.LoggedIn>().apply {
        prop(AuthEvent.LoggedIn::accessToken).isEqualTo("123")
        prop(AuthEvent.LoggedIn::refreshToken).isEqualTo("456")
      }
    }
  }

  @Test
  fun `auth repository failing the exchange, results in an error`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState(BankIdUiState.Loading, false)) {
      assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(
        LoginStatusResult.Pending(
          "test",
          LoginStatusResult.Pending.BankIdProperties("", "bankIdLiveQrCodeData", false),
        ),
      )
      assertThat(awaitItem().bankIdUiState)
        .isInstanceOf<BankIdUiState.HandlingBankId>()
        .apply {
          prop(BankIdUiState.HandlingBankId::bankIdLiveQrCodeData)
            .isNotNull()
            .prop(BankIdUiState.HandlingBankId.BankIdLiveQrCodeData::data)
            .isEqualTo("bankIdLiveQrCodeData")
          prop(BankIdUiState.HandlingBankId::statusMessage).isEqualTo("test")
        }
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("grant")))
      expectNoEvents()
      // Exchange fails
      authRepository.exchangeResponse.add(AuthTokenResult.Error.UnknownError("failed"))
      assertThat(awaitItem().bankIdUiState).isEqualTo(BankIdUiState.BankIdError("failed"))
      authTokenService.authEventTurbine.expectNoEvents()
    }
  }

  @Test
  fun `login status result failing, results in an error with the returned message`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState(BankIdUiState.Loading, false)) {
      assertThat(awaitItem().bankIdUiState).isEqualTo(BankIdUiState.Loading)
      authRepository.authAttemptResponse.add(
        AuthAttemptResult.BankIdProperties("", StatusUrl(""), ""),
      )
      authRepository.loginStatusResponse.add(
        LoginStatusResult.Pending(
          "pending",
          LoginStatusResult.Pending.BankIdProperties("", "bankIdLiveQrCodeData", false),
        ),
      )
      assertThat(awaitItem().bankIdUiState)
        .isInstanceOf<BankIdUiState.HandlingBankId>()
        .apply {
          prop(BankIdUiState.HandlingBankId::bankIdLiveQrCodeData)
            .isNotNull()
            .prop(BankIdUiState.HandlingBankId.BankIdLiveQrCodeData::data)
            .isEqualTo("bankIdLiveQrCodeData")
          prop(BankIdUiState.HandlingBankId::statusMessage).isEqualTo("pending")
        }
      authRepository.loginStatusResponse.add(LoginStatusResult.Failed("failed"))
      assertThat(awaitItem().bankIdUiState).isEqualTo(BankIdUiState.BankIdError("failed"))
      authTokenService.authEventTurbine.expectNoEvents()
    }
  }

  @Test
  fun `login status result succeeding, sends a loggedIn event`() = runTest {
    val authTokenService = TestAuthTokenService()
    val authRepository = FakeAuthRepository()
    val presenter: SwedishLoginPresenter = testSwedishLoginPresenter(authRepository, authTokenService)

    presenter.test(SwedishLoginUiState(BankIdUiState.Loading, false)) {
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

  @Test
  fun `pausing the collection and coming back after process death should continue the login process normally`() =
    runTest {
      val authRepository = FakeAuthRepository()
      val authTokenService = TestAuthTokenService()

      val successBankIdProperties = AuthAttemptResult.BankIdProperties("id", StatusUrl(""), "auto")
      testSwedishLoginPresenter(authRepository, authTokenService).test(
        SwedishLoginUiState(BankIdUiState.Loading, false),
      ) {
        assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
        authRepository.authAttemptResponse.add(successBankIdProperties)
        awaitUnchanged()
        authRepository.loginStatusResponse.add(LoginStatusResult.Pending("status", null))
        runCurrent()
        val handlingBankIdState = BankIdUiState.HandlingBankId(
          "status",
          BankIdUiState.HandlingBankId.AutoStartToken("auto"),
          null,
          false,
          true,
        )
        assertThat(awaitItem().bankIdUiState).isEqualTo(handlingBankIdState)
        sendEvent(SwedishLoginEvent.DidOpenBankIDApp)
        assertThat(awaitItem().bankIdUiState).isEqualTo(handlingBankIdState.copy(allowOpeningBankId = false))
        expectNoEvents()
      }

      // Process death happening here, we get a brand new presenter, with the same SavedStateHandle.
      // Note that with the SavedStateHandle.saveable APIs, the value is only saved into the handle when there is a
      // real event of process death which instructs viewmodel-savedstate to trigger the save state. This is either not
      // yet possible to do in unit tests, or is quite hard to get right, so we need to hack it a little but by
      // manually populating the SavedStateHandle ourselves with the right values, simulating what would happen in a
      // process death scenario.
      // When that artifact goes KMP it will most likely provide a much easier way to integrate process death scenarios
      // into our tests.
      val savedStateHandleWithSavedBankIdData = savedStateHandlePopulatedWith(successBankIdProperties)
      testSwedishLoginPresenter(authRepository, authTokenService, savedStateHandleWithSavedBankIdData).test(
        SwedishLoginUiState(BankIdUiState.Loading, false),
      ) {
        assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
        authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("1234")))
        expectNoEvents()
        authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
        assertThat((authTokenService.authEventTurbine.awaitItem() as AuthEvent.LoggedIn).accessToken).isEqualTo("123")
        val itemAfterSuccessfulResponse = awaitItem()
        assertThat(itemAfterSuccessfulResponse.bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
        assertThat(itemAfterSuccessfulResponse.navigateToLoginScreen).isTrue()

        // expect to never have asked for a new login attempt since we had an ongoing login attempt
        authRepository.authAttemptResponse.expectNoEvents()
      }
    }

  @Test
  fun `coming from process death should continue polling for a success coming later`() = runTest {
    val authRepository = FakeAuthRepository()
    val authTokenService = TestAuthTokenService()

    val successBankIdProperties = AuthAttemptResult.BankIdProperties("id", StatusUrl(""), "auto")
    val savedStateHandleWithSavedBankIdData = savedStateHandlePopulatedWith(successBankIdProperties)
    testSwedishLoginPresenter(authRepository, authTokenService, savedStateHandleWithSavedBankIdData).test(
      SwedishLoginUiState(BankIdUiState.Loading, false),
    ) {
      assertThat(awaitItem().bankIdUiState).isInstanceOf<BankIdUiState.Loading>()
      authRepository.loginStatusResponse.add(LoginStatusResult.Pending("status", null))
      assertThat(awaitItem().bankIdUiState).isEqualTo(
        BankIdUiState.HandlingBankId(
          "status",
          BankIdUiState.HandlingBankId.AutoStartToken(successBankIdProperties.autoStartToken),
          null,
          false,
          true,
        ),
      )
      authRepository.loginStatusResponse.add(LoginStatusResult.Completed(AuthorizationCodeGrant("1234")))
      expectNoEvents()
      authRepository.exchangeResponse.add(AuthTokenResult.Success(AccessToken("123", 90), RefreshToken("456", 90)))
      assertThat((authTokenService.authEventTurbine.awaitItem() as AuthEvent.LoggedIn).accessToken).isEqualTo("123")
      val itemAfterSuccessfulResponse = awaitItem()
      assertThat(itemAfterSuccessfulResponse.bankIdUiState).isEqualTo(
        BankIdUiState.HandlingBankId(
          "status",
          BankIdUiState.HandlingBankId.AutoStartToken(successBankIdProperties.autoStartToken),
          null,
          false,
          true,
        ),
      )
      assertThat(itemAfterSuccessfulResponse.navigateToLoginScreen).isTrue()

      // expect to never have asked for a new login attempt since we had an ongoing login attempt
      authRepository.authAttemptResponse.expectNoEvents()
    }
  }

  private fun testSwedishLoginPresenter(
    authRepository: AuthRepository,
    authTokenService: AuthTokenService = TestAuthTokenService(),
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
  ): SwedishLoginPresenter {
    return SwedishLoginPresenter(
      authTokenService,
      authRepository,
      object : DemoManager {
        override fun isDemoMode(): Flow<Boolean> = flowOf(false)

        override suspend fun setDemoMode(demoMode: Boolean) {}
      },
      savedStateHandle,
    )
  }

  /**
   * The saveable APIs expect the bundle inside the key we provide to be specifically
   * `"value" to mutableStateof(...)`. The "value" part is hardcoded, and the second parameter needs to be
   * `mutableState` which contains inside the raw information of how our saver knows to restore from.
   * In our case the saver implementation is done by a listSaver saving the BankIdProperties in a list.
   */
  private fun savedStateHandlePopulatedWith(bankIdProperties: AuthAttemptResult.BankIdProperties): SavedStateHandle {
    return SavedStateHandle(
      mapOf(
        BankIdPropertiesSaver.ID to bundleOf(
          "value" to mutableStateOf(
            listOf(
              bankIdProperties.id,
              bankIdProperties.statusUrl.url,
              bankIdProperties.autoStartToken,
            ),
          ),
        ),
      ),
    )
  }
}
