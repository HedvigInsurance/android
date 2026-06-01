package com.hedvig.android.feature.payments.di

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

@ContributesTo(AppScope::class)
interface PaymentsMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetUpcomingPaymentUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetUpcomingPaymentUseCaseImpl,
    demoImpl: GetUpcomingPaymentUseCaseDemo,
  ): Provider<GetUpcomingPaymentUseCase> = GetUpcomingPaymentUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideGetShouldShowPayoutUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetShouldShowPayoutUseCaseImpl,
    demoImpl: GetShouldShowPayoutUseCaseDemo,
  ): Provider<GetShouldShowPayoutUseCase> = GetShouldShowPayoutUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
