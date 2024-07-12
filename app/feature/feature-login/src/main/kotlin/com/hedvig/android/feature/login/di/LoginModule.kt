package com.hedvig.android.feature.login.di

import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.login.genericauth.GenericAuthViewModel
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.navigation.LoginDestinations
import com.hedvig.android.feature.login.otpinput.OtpInputViewModel
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.set.SetMarketUseCase
import com.hedvig.authlib.AuthRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
  viewModel<MarketingViewModel> {
    MarketingViewModel(get<MarketManager>(), get<LanguageService>(), get<SetMarketUseCase>())
  }
  viewModel<SwedishLoginViewModel> {
    SwedishLoginViewModel(
      authTokenService = get<AuthTokenService>(),
      authRepository = get<AuthRepository>(),
      demoManager = get<DemoManager>(),
      savedStateHandle = get<SavedStateHandle>(),
    )
  }

  viewModel<GenericAuthViewModel> {
    GenericAuthViewModel(get<MarketManager>(), get<AuthRepository>())
  }
  viewModel<OtpInputViewModel> { (otpInformation: LoginDestinations.OtpInput.OtpInformation) ->
    OtpInputViewModel(
      otpInformation.verifyUrl,
      otpInformation.resendUrl,
      otpInformation.credential,
      get<AuthTokenService>(),
      get<AuthRepository>(),
    )
  }
}
