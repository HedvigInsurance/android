package com.hedvig.android.feature.payments.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCase
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCaseDemo
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCaseProvider
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseDemo
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@ContributesTo(AppScope::class)
interface PaymentsMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetUpcomingPaymentUseCaseProvider(
    demoManager: DemoManager,
    apolloClient: ApolloClient,
    clock: Clock,
  ): Provider<GetUpcomingPaymentUseCase> = GetUpcomingPaymentUseCaseProvider(
    demoManager = demoManager,
    prodImpl = GetUpcomingPaymentUseCaseImpl(apolloClient, clock),
    demoImpl = GetUpcomingPaymentUseCaseDemo(clock),
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideGetShouldShowPayoutUseCaseProvider(
    demoManager: DemoManager,
    apolloClient: ApolloClient,
  ): Provider<GetShouldShowPayoutUseCase> = GetShouldShowPayoutUseCaseProvider(
    demoManager = demoManager,
    prodImpl = GetShouldShowPayoutUseCaseImpl(apolloClient),
    demoImpl = GetShouldShowPayoutUseCaseDemo(),
  )
}
