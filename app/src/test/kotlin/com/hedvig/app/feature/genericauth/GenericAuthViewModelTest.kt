package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class GenericAuthViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private var otpResult: CreateOtpResult = CreateOtpResult.Success("testId")

  private val viewModel = GenericAuthViewModel(
    createOtpAttemptUseCase = object : CreateOtpAttemptUseCase {
      override suspend fun invoke(email: String): CreateOtpResult {
        delay(100.milliseconds)
        return otpResult
      }
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
    assertThat(viewModel.viewState.value.otpId).isEqualTo(null)

    viewModel.setInput("valid@email.com")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.emailInput).isEqualTo("valid@email.com")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.otpId).isEqualTo((otpResult as CreateOtpResult.Success).id)
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
    assertThat(viewModel.viewState.value.otpId).isNull()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.otpId).isNotNull()
  }
}
