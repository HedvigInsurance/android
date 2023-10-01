package com.hedvig.android.feature.login.di

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.authlib.AuthRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
  viewModel<MarketingViewModel> {
    MarketingViewModel(get<MarketManager>(), get<LanguageService>())
  }
  viewModel<SwedishLoginViewModel> {
    SwedishLoginViewModel(get<AuthTokenService>(), get<AuthRepository>())
  }
}
