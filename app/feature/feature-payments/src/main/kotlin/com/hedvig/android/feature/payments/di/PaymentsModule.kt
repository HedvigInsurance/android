package com.hedvig.android.feature.payments.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.feature.payments.overview.PaymentOverviewViewModel
import com.hedvig.android.feature.payments.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.data.AddDiscountUseCaseImpl
import com.hedvig.android.feature.payments.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.data.GetUpcomingPaymentUseCaseImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val payments2Module = module {
  single<GetUpcomingPaymentUseCase> {
    GetUpcomingPaymentUseCaseImpl(get<ApolloClient>())
  }

  single<AddDiscountUseCase> {
    AddDiscountUseCaseImpl(
      get<ApolloClient>(),
      get<NetworkCacheManager>(),
    )
  }

  viewModel<PaymentOverviewViewModel> {
    PaymentOverviewViewModel(
      get<GetUpcomingPaymentUseCase>(),
      get<AddDiscountUseCase>(),
    )
  }
}
