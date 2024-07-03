package com.hedvig.android.feature.login.genericauth

// import com.hedvig.android.auth.test.FakeAuthRepository

// TODO("todo does not work without authlib")
// class OtpInformationViewModelTest {
//  @get:Rule
//  val mainCoroutineRule = MainCoroutineRule()
//
//  @get:Rule
//  val testLogcatLogger = TestLogcatLoggingRule()
//
//  @Test
//  fun testNetworkError() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//
//    viewModel.submitCode("123456")
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
//    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
//
//    authRepository.submitOtpResponse.add(SubmitOtpResult.Error("Error"))
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo("Error")
//  }
//
//  @Test
//  fun testDismissNetworkError() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//    val errorMessage = "Error"
//
//    viewModel.submitCode("123456")
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
//    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//
//    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(errorMessage))
//    runCurrent()
//    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(errorMessage)
//    viewModel.dismissError()
//    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(null)
//  }
//
//  @Test
//  fun testOtpError() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//
//    viewModel.submitCode("123456")
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
//    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//
//    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
//    runCurrent()
//    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()
//  }
//
//  @Test
//  fun testOtpSuccess() = runTest {
//    val authRepository = FakeAuthRepository()
//    val authTokenService = TestAuthTokenService()
//    val viewModel = testViewModel(authRepository, authTokenService)
//
//    viewModel.events.test {
//      viewModel.submitCode("123456")
//      runCurrent()
//      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
//      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//
//      authRepository.submitOtpResponse.add(SubmitOtpResult.Success(AuthorizationCodeGrant("")))
//      authRepository.exchangeResponse.add(AuthTokenResult.Success(accessToken, refreshToken))
//      runCurrent()
//
//      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//      val authEvent = authTokenService.authEventTurbine.awaitItem()
//      assertThat(authEvent).isInstanceOf<AuthEvent.LoggedIn>().run {
//        prop(AuthEvent.LoggedIn::accessToken).isEqualTo(accessToken.token)
//        prop(AuthEvent.LoggedIn::refreshToken).isEqualTo(refreshToken.token)
//      }
//      val event = awaitItem()
//      assertThat(event).isEqualTo(OtpInputViewModel.Event.Success(accessToken.token))
//    }
//  }
//
//  @Test
//  fun testResendError() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//    val errorMessage = "Error"
//
//    viewModel.resendCode()
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)
//
//    authRepository.resendOtpResponse.add(ResendOtpResult.Error(errorMessage))
//    runCurrent()
//    assertThat(viewModel.viewState.value.networkErrorMessage).isEqualTo(errorMessage)
//  }
//
//  @Test
//  fun testResend() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//
//    viewModel.events.test {
//      viewModel.resendCode()
//      runCurrent()
//      assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(true)
//
//      authRepository.resendOtpResponse.add(ResendOtpResult.Success)
//      runCurrent()
//      assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//      val event = awaitItem()
//      assertThat(event).isEqualTo(OtpInputViewModel.Event.CodeResent)
//    }
//  }
//
//  @Test
//  fun testSetInput() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//    viewModel.setInput("1")
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//    assertThat(viewModel.viewState.value.input).isEqualTo("1")
//
//    viewModel.setInput("12")
//    assertThat(viewModel.viewState.value.input).isEqualTo("12")
//
//    viewModel.setInput("123")
//    assertThat(viewModel.viewState.value.input).isEqualTo("123")
//  }
//
//  @Test
//  fun `updating the input after getting an error should clear the error`() = runTest {
//    val authRepository = FakeAuthRepository()
//    val viewModel = testViewModel(authRepository, TestAuthTokenService())
//
//    viewModel.submitCode("111111")
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(true)
//    assertThat(viewModel.viewState.value.loadingResend).isEqualTo(false)
//    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()
//
//    authRepository.submitOtpResponse.add(SubmitOtpResult.Error(""))
//    runCurrent()
//    assertThat(viewModel.viewState.value.loadingCode).isEqualTo(false)
//    assertThat(viewModel.viewState.value.networkErrorMessage).isNotNull()
//
//    viewModel.setInput("1")
//    assertThat(viewModel.viewState.value.networkErrorMessage).isNull()
//  }
//
//  private fun testViewModel(
//    authRepository: FakeAuthRepository,
//    authTokenService: TestAuthTokenService,
//  ): OtpInputViewModel {
//    return OtpInputViewModel(
//      verifyUrl = "verifytest",
//      resendUrl = "resendtest",
//      credential = "test@email.com",
//      authTokenService = authTokenService,
//      authRepository = authRepository,
//    )
//  }
//
//  companion object {
//    private val accessToken = AccessToken("testAccessToken", 100)
//    private val refreshToken = RefreshToken("testRefreshToken", 100)
//  }
// }
