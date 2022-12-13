package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.Grant
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.RevokeResult
import com.hedvig.authlib.StatusUrl
import com.hedvig.authlib.SubmitOtpResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class GenericAuthViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val viewModel = GenericAuthViewModel(
    authRepository = object : AuthRepository {
      override suspend fun startLoginAttempt(
        loginMethod: LoginMethod,
        market: String,
        personalNumber: String?,
        email: String?,
      ): AuthAttemptResult {
        delay(100.milliseconds)
        return AuthAttemptResult.OtpProperties(
          id = "123",
          statusUrl = StatusUrl("testStatusUrl"),
          resendUrl = "resendUrl",
          verifyUrl = "verifyUrl",
        )
      }

      override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
        TODO("Not yet implemented")
      }

      override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
        TODO("Not yet implemented")
      }

      override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
        TODO("Not yet implemented")
      }

      override suspend fun exchange(grant: Grant): AuthTokenResult {
        TODO("Not yet implemented")
      }

      override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
        TODO("Not yet implemented")
      }

      override suspend fun migrateOldToken(token: String): AuthTokenResult {
        TODO("Not yet implemented")
      }

      override suspend fun revoke(token: String): RevokeResult {
        TODO("Not yet implemented")
      }
    },
    marketManager = object : MarketManager {
      override val enabledMarkets: List<Market>
        get() = listOf(Market.SE)
      override var market: Market? = Market.SE
    },
  )

  @Test
  fun `set input should be set to view state`() = runTest {
    viewModel.setInput("test")
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("test")
  }

  @Test
  fun `otp id should be present when successfully submitting valid email`() = runTest {
    viewModel.setInput("invalid email..")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("invalid email..")
    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewState.TextFieldError.INVALID_EMAIL)
    assertThat(viewModel.viewState.value.verifyUrl).isEqualTo(null)

    viewModel.setInput("valid@email.com")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("valid@email.com")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.verifyUrl).isEqualTo("verifyUrl")
  }

  @Test
  fun `clear should remove input and error state`() = runTest {
    viewModel.setInput("invalid email.. ")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("invalid email.. ")
    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewState.TextFieldError.INVALID_EMAIL)

    viewModel.clear()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
  }

  @Test
  fun `should load when submitting valid email`() = runTest {
    viewModel.setInput("valid@email.com")
    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
    viewModel.submitEmail()
    runCurrent()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("valid@email.com")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.loading).isEqualTo(true)
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
  }

  @Test
  fun `should consider an email with trailing or leading whitespaces as valid`() = runTest {
    val input = " valid@email.com "
    viewModel.setInput(input)
    viewModel.submitEmail()
    runCurrent()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo(input)
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.verifyUrl).isNull()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.verifyUrl).isNotNull()
  }
}
