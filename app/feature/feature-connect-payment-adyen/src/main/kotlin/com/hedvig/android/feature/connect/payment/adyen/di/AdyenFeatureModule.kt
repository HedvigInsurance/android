package com.hedvig.android.feature.connect.payment.adyen.di

import com.hedvig.android.feature.connect.payment.adyen.AdyenViewModel
import com.hedvig.android.feature.connect.payment.adyen.data.GetAdyenPaymentUrlUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val adyenFeatureModule = module {
  single<GetAdyenPaymentUrlUseCase> {
    GetAdyenPaymentUrlUseCase() // todo add REST service here
  }
  viewModel<AdyenViewModel> {
    AdyenViewModel(get<GetAdyenPaymentUrlUseCase>())
  }
}
