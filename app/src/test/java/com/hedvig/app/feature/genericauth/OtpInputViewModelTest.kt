package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpResult
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ResendOtpResult
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OtpInputViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
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
                delay(100)
                return otpResult
            }
        },
        reSendOtpCodeUseCase = object : ReSendOtpCodeUseCase {
            override suspend fun invoke(credential: String): ResendOtpResult {
                delay(100)
                return resendOtpResult
            }
        }
    )

    @ExperimentalCoroutinesApi
    @Test
    fun testNetworkError() = mainCoroutineRule.dispatcher.runBlockingTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        viewModel.submitCode("123456")

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDismissNetworkError() = mainCoroutineRule.dispatcher.runBlockingTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        viewModel.submitCode("123456")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")

        viewModel.dismissError()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testOtpError() = mainCoroutineRule.dispatcher.runBlockingTest {
        otpResult = OtpResult.Error.OtpError.Expired

        viewModel.submitCode("123456")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.otpError).isEqualTo(OtpResult.Error.OtpError.Expired)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testOtpSuccess() = mainCoroutineRule.dispatcher.runBlockingTest {
        otpResult = OtpResult.Success("testOtpSuccess")

        viewModel.submitCode("123456")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)

        advanceUntilIdle()
        assertThat(viewModel.events.first()).isEqualTo(OtpInputViewModel.Event.Success("testOtpSuccess"))
        assertThat(authToken).isEqualTo("testOtpSuccess")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testResendError() = mainCoroutineRule.dispatcher.runBlockingTest {
        resendOtpResult = ResendOtpResult.Error("Error")

        viewModel.resendCode()
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testResend() = mainCoroutineRule.dispatcher.runBlockingTest {
        resendOtpResult = ResendOtpResult.Success("auth")

        viewModel.resendCode()
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
        assertThat(viewModel.events.first()).isEqualTo(OtpInputViewModel.Event.CodeResent)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testSetInput() = mainCoroutineRule.dispatcher.runBlockingTest {
        viewModel.setInput("1")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
        assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
        assertThat(viewModel.viewState.value.input).isEqualTo("1")

        viewModel.setInput("12")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.input).isEqualTo("12")

        viewModel.setInput("123")
        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.input).isEqualTo("123")
    }
}
