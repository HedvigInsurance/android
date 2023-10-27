package com.hedvig.android.feature.connect.payment.adyen.di

import com.hedvig.android.feature.connect.payment.adyen.AdyenViewModel
import com.hedvig.android.feature.connect.payment.adyen.data.GetAdyenPaymentUrlUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.authlib.connectpayment.PaymentRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val adyenFeatureModule = module {
  single<GetAdyenPaymentUrlUseCase> {
    GetAdyenPaymentUrlUseCase(
      get<PaymentRepository>(),
      get<LanguageService>(),
    )
  }
  viewModel<AdyenViewModel> {
    AdyenViewModel(get<GetAdyenPaymentUrlUseCase>())
  }
}
