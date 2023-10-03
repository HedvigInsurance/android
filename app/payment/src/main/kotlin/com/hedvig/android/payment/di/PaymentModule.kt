package com.hedvig.android.payment.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.payment.PaymentRepositoryDemo
import com.hedvig.android.payment.PaymentRepositoryImpl
import org.koin.dsl.module

val paymentModule = module {
  single<PaymentRepositoryImpl> { PaymentRepositoryImpl(get<ApolloClient>(giraffeClient), get()) }
  single<PaymentRepositoryDemo> { PaymentRepositoryDemo() }
  single {
    PaymentRepositoryProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<PaymentRepositoryImpl>(),
      demoImpl = get<PaymentRepositoryDemo>(),
    )
  }
}
