package com.hedvig.android.payment.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.payment.PaymentDemoRepositoryImpl
import com.hedvig.android.payment.PaymentRepository
import com.hedvig.android.payment.PaymentRepositoryImpl
import org.koin.dsl.module

val paymentModule = module {
  single<PaymentRepository> { PaymentRepositoryImpl(get<ApolloClient>(giraffeClient), get()) }
}

val paymentDemoModule = module {
  single<PaymentRepository> { PaymentDemoRepositoryImpl() }
}
