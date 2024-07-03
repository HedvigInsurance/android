package com.hedvig.android.feature.login.genericauth

// TODO("todo does not work without authlib")
// class GenericAuthViewModelTest {
//  @get:Rule
//  val mainCoroutineRule = MainCoroutineRule()
//
//  private val viewModel = GenericAuthViewModel(
//    authRepository = object : AuthRepository {
//      override suspend fun startLoginAttempt(
//        loginMethod: LoginMethod,
//        market: OtpMarket,
//        personalNumber: String?,
//        email: String?,
//      ): AuthAttemptResult {
//        delay(100.milliseconds)
//        return AuthAttemptResult.OtpProperties(
//          id = "123",
//          statusUrl = StatusUrl("testStatusUrl"),
//          resendUrl = "resendUrl",
//          verifyUrl = "verifyUrl",
//          maskedEmail = null,
//        )
//      }
//
//      override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
//        error("Not yet implemented")
//      }
//
//      override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
//        error("Not yet implemented")
//      }
//
//      override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
//        error("Not yet implemented")
//      }
//
//      override suspend fun exchange(grant: Grant): AuthTokenResult {
//        error("Not yet implemented")
//      }
//
//      override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
//        error("Not yet implemented")
//      }
//
//      override suspend fun revoke(token: String): RevokeResult {
//        error("Not yet implemented")
//      }
//    },
//    marketManager = FakeMarketManager(),
//  )
//
//  @Test
//  fun `set input should be set to view state`() = runTest {
//    viewModel.setEmailInput("test")
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("test")
//  }
//
//  @Test
//  fun `otp id should be present when successfully submitting valid email`() = runTest {
//    viewModel.setEmailInput("invalid email..")
//    viewModel.submitEmail()
//    advanceUntilIdle()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("invalid email..")
//    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.InvalidEmail)
//    assertThat(viewModel.viewState.value.verifyUrl).isEqualTo(null)
//
//    viewModel.setEmailInput("valid@email.com")
//    viewModel.submitEmail()
//    advanceUntilIdle()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("valid@email.com")
//    assertThat(viewModel.viewState.value.error).isEqualTo(null)
//    assertThat(viewModel.viewState.value.verifyUrl).isEqualTo("verifyUrl")
//  }
//
//  @Test
//  fun `clear should remove input and error state`() = runTest {
//    viewModel.setEmailInput("invalid email.. ")
//    viewModel.submitEmail()
//    advanceUntilIdle()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("invalid email.. ")
//    assertThat(viewModel.viewState.value.error).isEqualTo(GenericAuthViewState.TextFieldError.Other.InvalidEmail)
//
//    viewModel.clear()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("")
//    assertThat(viewModel.viewState.value.error).isEqualTo(null)
//  }
//
//  @Test
//  fun `should load when submitting valid email`() = runTest {
//    viewModel.setEmailInput("valid@email.com")
//    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
//    viewModel.submitEmail()
//    runCurrent()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo("valid@email.com")
//    assertThat(viewModel.viewState.value.error).isEqualTo(null)
//    assertThat(viewModel.viewState.value.loading).isEqualTo(true)
//    advanceUntilIdle()
//    assertThat(viewModel.viewState.value.loading).isEqualTo(false)
//  }
//
//  @Test
//  fun `should consider an email with trailing or leading whitespaces as valid`() = runTest {
//    val input = " valid@email.com "
//    viewModel.setEmailInput(input)
//    viewModel.submitEmail()
//    runCurrent()
//    assertThat(viewModel.viewState.value.emailInput).isEqualTo(input)
//    assertThat(viewModel.viewState.value.error).isEqualTo(null)
//    assertThat(viewModel.viewState.value.verifyUrl).isNull()
//    advanceUntilIdle()
//    assertThat(viewModel.viewState.value.verifyUrl).isNotNull()
//  }
// }
