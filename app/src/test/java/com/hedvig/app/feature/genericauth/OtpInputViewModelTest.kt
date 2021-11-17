package com.hedvig.app.feature.genericauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpResult
import com.hedvig.app.feature.genericauth.otpinput.ReSendOtpCodeUseCase
import com.hedvig.app.feature.genericauth.otpinput.ResendOtpResult
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OtpInputViewModelTest {

    private lateinit var viewModel: OtpInputViewModel
    private var authToken: String? = "testToken"

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private var otpResult: OtpResult = OtpResult.Success("authtest")
    private var resendOtpResult: ResendOtpResult = ResendOtpResult.Success("authtest")

    @Before
    fun setup() {
        viewModel = OtpInputViewModel(
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
                    return otpResult
                }
            },
            reSendOtpCodeUseCase = object : ReSendOtpCodeUseCase {
                override suspend fun invoke(credential: String): ResendOtpResult {
                    return resendOtpResult
                }
            }
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testNetworkError() = runBlockingTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        viewModel.submitCode("123456")

        assertThat(results[1].loadingCode).isEqualTo(true)
        assertThat(results[1].loadingResend).isEqualTo(false)
        assertThat(results[2].networkErrorMessage).isEqualTo("Error")

        job.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDismissNetworkError() = runBlockingTest {
        otpResult = OtpResult.Error.NetworkError("Error")

        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        viewModel.submitCode("123456")

        assertThat(results[1].loadingCode).isEqualTo(true)
        assertThat(results[1].loadingResend).isEqualTo(false)
        assertThat(results[2].networkErrorMessage).isEqualTo("Error")

        viewModel.dismissError()

        assertThat(results[3].networkErrorMessage).isEqualTo(null)

        job.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testOtpError() = runBlockingTest {
        otpResult = OtpResult.Error.OtpError.Expired

        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        viewModel.submitCode("123456")

        assertThat(results[1].loadingCode).isEqualTo(true)
        assertThat(results[1].loadingResend).isEqualTo(false)
        assertThat(results[2].otpError).isEqualTo(R.string.login_code_input_error_msg_expired)

        job.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testOtpSuccess() = runBlockingTest {
        otpResult = OtpResult.Success("testOtpSuccess")

        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        val events = mutableListOf<OtpInputViewModel.Event>()
        val job2 = launch {
            viewModel.events.toList(events)
        }

        viewModel.submitCode("123456")

        assertThat(results[1].loadingCode).isEqualTo(true)
        assertThat(results[1].loadingResend).isEqualTo(false)
        assertThat(events[0]).isEqualTo(OtpInputViewModel.Event.Success("testOtpSuccess"))
        assertThat(authToken).isEqualTo("testOtpSuccess")

        job.cancel()
        job2.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testResendError() = runBlockingTest {
        resendOtpResult = ResendOtpResult.Error("Error")

        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        viewModel.resendCode()

        assertThat(results[1].loadingCode).isEqualTo(false)
        assertThat(results[1].loadingResend).isEqualTo(true)
        assertThat(results[2].networkErrorMessage).isEqualTo("Error")

        job.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testResend() = runBlockingTest {
        resendOtpResult = ResendOtpResult.Success("auth")

        val viewStateResults = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(viewStateResults)
        }

        val events = mutableListOf<OtpInputViewModel.Event>()
        val job2 = launch {
            viewModel.events.toList(events)
        }

        viewModel.resendCode()

        assertThat(viewStateResults[1].loadingCode).isEqualTo(false)
        assertThat(viewStateResults[1].loadingResend).isEqualTo(true)
        assertThat(viewStateResults[2].loadingResend).isEqualTo(false)
        assertThat(events[0]).isEqualTo(OtpInputViewModel.Event.CodeResent)

        job.cancel()
        job2.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testSetInput() = runBlockingTest {
        val results = mutableListOf<OtpInputViewModel.ViewState>()
        val job = launch {
            viewModel.viewState.toList(results)
        }

        viewModel.setInput("1")
        viewModel.setInput("12")
        viewModel.setInput("123")

        assertThat(results[1].loadingCode).isEqualTo(false)
        assertThat(results[1].loadingResend).isEqualTo(false)
        assertThat(results[1].input).isEqualTo("1")
        assertThat(results[2].input).isEqualTo("12")
        assertThat(results[3].input).isEqualTo("123")

        job.cancel()
    }
}
