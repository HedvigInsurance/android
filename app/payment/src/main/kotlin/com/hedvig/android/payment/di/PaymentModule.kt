package com.hedvig.android.payment.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.payment.PaymentRepository
import org.koin.dsl.module

val paymentModule = module {
  single<PaymentRepository> { PaymentRepository(get<ApolloClient>(giraffeClient), get()) }
}
