package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.auth.AuthAttemptResult
import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.AuthorizationCode
import com.hedvig.android.auth.LoginMethod
import com.hedvig.android.auth.LoginStatusResult
import com.hedvig.android.auth.LogoutResult
import com.hedvig.android.auth.RefreshCode
import com.hedvig.android.auth.ResendOtpResult
import com.hedvig.android.auth.StatusUrl
import com.hedvig.android.auth.SubmitOtpResult
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow

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

      override suspend fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult {
        TODO("Not yet implemented")
      }

      override suspend fun logout(refreshCode: RefreshCode): LogoutResult {
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
