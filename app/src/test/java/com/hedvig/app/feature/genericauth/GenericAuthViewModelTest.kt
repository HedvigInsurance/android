package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
    assertThat(viewModel.viewState.value.input).isEqualTo("test")
  }

  @Test
  fun `otp id should be present when successfully submitting valid email`() = runTest {
    viewModel.setInput("invalid email..")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.input).isEqualTo("invalid email..")
    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewModel.ViewState.TextFieldError.INVALID_EMAIL)
    assertThat(viewModel.viewState.value.otpId).isEqualTo(null)

    viewModel.setInput("valid@email.com")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.input).isEqualTo("valid@email.com")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.otpId).isEqualTo((otpResult as CreateOtpResult.Success).id)
  }

  @Test
  fun `clear should remove input and error state`() = runTest {
    viewModel.setInput("invalid email..")
    viewModel.submitEmail()
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.input).isEqualTo("invalid email..")
    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewModel.ViewState.TextFieldError.INVALID_EMAIL)

    viewModel.clear()
    assertThat(viewModel.viewState.value.input).isEqualTo("")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
  }

  @Test
  fun `should load when submitting valid email`() = runTest {
    viewModel.setInput("valid@email.com")
    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
    viewModel.submitEmail()
    advanceTimeBy(10)
    assertThat(viewModel.viewState.value.input).isEqualTo("valid@email.com")
    assertThat(viewModel.viewState.value.error).isEqualTo(null)
    assertThat(viewModel.viewState.value.loading).isEqualTo(true)
    advanceUntilIdle()
    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
  }
}
