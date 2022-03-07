package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpResult
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ResendOtpResult
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.util.coroutines.StandardTestDispatcherAsMainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class OtpInputViewModelTest {

    @get:Rule
    val standardTestDispatcherAsMainDispatcherRule = StandardTestDispatcherAsMainDispatcherRule()

    private var authToken: String? = "testToken"

    private var otpResult: OtpResult = OtpResult.Success("authtest")
    private var resendOtpResult: ResendOtpResult = ResendOtpResult.Success("authtest")

    private val viewModel = OtpInputViewModel(
        otpId = "testId",
        credential = "test@email.com",
        authenticationTokenService = object : AuthenticationTokenService {
            override var authenticationToken: String?
                get() = authToken
                set(value) {
                    authToken = value
                }
        },
        sendOtpCodeUseCase = object : SendOtpCodeUseCase {
            override suspend fun invoke(otpId: String, otpCode: String): OtpResult {
                delay(100.milliseconds)
                return otpResult
            }
        },
        reSendOtpCodeUseCase = object : ReSendOtpCodeUseCase {
            override suspend fun invoke(credential: String): ResendOtpResult {
                delay(100.milliseconds)
                return resendOtpResult
            }
        }
    )

    @Test
    fun testNetworkError() = runTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        viewModel.submitCode("123456")

        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
    }

    @Test
    fun testDismissNetworkError() = runTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        viewModel.submitCode("123456")
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
        viewModel.dismissError()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
    }

    @Test
    fun testOtpError() = runTest {
        otpResult = OtpResult.Error.OtpError.Expired

        viewModel.submitCode("123456")
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.otpError).isEqualTo(OtpResult.Error.OtpError.Expired)
    }

    @Test
    fun testOtpSuccess() = runTest {
        otpResult = OtpResult.Success("testOtpSuccess")

        val events = mutableListOf<OtpInputViewModel.Event>()
        val eventCollectingJob = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.submitCode("123456")
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(authToken).isEqualTo("testOtpSuccess")
        assertThat(events.size).isEqualTo(1)
        assertThat(events.first()).isEqualTo(OtpInputViewModel.Event.Success("testOtpSuccess"))
        eventCollectingJob.cancel()
    }

    @Test
    fun testResendError() = runTest {
        resendOtpResult = ResendOtpResult.Error("Error")

        viewModel.resendCode()
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
    }

    @Test
    fun testResend() = runTest {
        resendOtpResult = ResendOtpResult.Success("auth")

        val events = mutableListOf<OtpInputViewModel.Event>()
        val eventCollectingJob = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.resendCode()
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
        assertThat(events.size).isEqualTo(1)
        assertThat(events.first()).isEqualTo(OtpInputViewModel.Event.CodeResent)
        eventCollectingJob.cancel()
    }

    @Test
    fun testSetInput() = runTest {
        viewModel.setInput("1")
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
        assertThat(viewModel.viewState.value.input).isEqualTo("1")

        viewModel.setInput("12")
        assertThat(viewModel.viewState.value.input).isEqualTo("12")

        viewModel.setInput("123")
        assertThat(viewModel.viewState.value.input).isEqualTo("123")
    }
}
