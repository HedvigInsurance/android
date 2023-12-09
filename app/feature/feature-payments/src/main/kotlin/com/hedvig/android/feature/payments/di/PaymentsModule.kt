package com.hedvig.android.feature.payments.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.payments.PaymentViewModel
import com.hedvig.android.feature.payments.data.PaymentRepositoryDemo
import com.hedvig.android.feature.payments.data.PaymentRepositoryImpl
import com.hedvig.android.feature.payments.data.PaymentRepositoryProvider
import com.hedvig.android.feature.payments.history.PaymentHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val paymentsModule = module {
  viewModel<PaymentViewModel> {
    PaymentViewModel(get<PaymentRepositoryProvider>())
  }
  viewModel<PaymentHistoryViewModel> {
    PaymentHistoryViewModel(
      get<PaymentRepositoryProvider>(),
    )
  }

  single<PaymentRepositoryImpl> {
    PaymentRepositoryImpl(
      apolloClient = get<ApolloClient>(),
    )
  }
  single<PaymentRepositoryDemo> { PaymentRepositoryDemo() }
  single {
    PaymentRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<PaymentRepositoryImpl>(),
      demoImpl = get<PaymentRepositoryDemo>(),
    )
  }
}
