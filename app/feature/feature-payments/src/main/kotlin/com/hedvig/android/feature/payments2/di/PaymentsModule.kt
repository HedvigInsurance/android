package com.hedvig.android.feature.payments2.di

import com.hedvig.android.feature.payments2.PaymentOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val payments2Module = module {
  viewModel<PaymentOverviewViewModel> {
    PaymentOverviewViewModel()
  }
}
