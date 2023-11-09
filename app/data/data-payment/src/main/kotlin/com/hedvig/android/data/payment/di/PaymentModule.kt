package com.hedvig.android.data.payment.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.payment.PaymentRepositoryDemo
import com.hedvig.android.data.payment.PaymentRepositoryImpl
import org.koin.dsl.module

val paymentModule = module {
  single<PaymentRepositoryImpl> {
    PaymentRepositoryImpl(
      giraffeApolloClient = get<ApolloClient>(giraffeClient),
      octopusApolloClient = get<ApolloClient>(octopusClient),
      languageService = get(),
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
