package com.hedvig.android.feature.payments2.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.feature.payments2.PaymentOverviewViewModel
import com.hedvig.android.feature.payments2.data.AddDiscountUseCase
import com.hedvig.android.feature.payments2.data.AddDiscountUseCaseImpl
import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCaseImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val payments2Module = module {
  single<GetUpcomingPaymentUseCase> {
    GetUpcomingPaymentUseCaseImpl(get<ApolloClient>(octopusClient))
  }

  single<AddDiscountUseCase> {
    AddDiscountUseCaseImpl(
      get<ApolloClient>(octopusClient),
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
