package com.hedvig.android.notification.badge.data.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceImpl
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceProvider
import com.hedvig.android.notification.badge.data.crosssell.home.DemoCrossSellHomeNotificationService
import com.hedvig.android.notification.badge.data.payment.DemoMissedPaymentNotificationService
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationServiceImpl
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationServiceProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
internal interface NotificationBadgeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideMissedPaymentNotificationServiceProvider(
    demoManager: DemoManager,
    prodImpl: MissedPaymentNotificationServiceImpl,
    demoImpl: DemoMissedPaymentNotificationService,
  ): MissedPaymentNotificationServiceProvider = MissedPaymentNotificationServiceProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideCrossSellHomeNotificationServiceProvider(
    demoManager: DemoManager,
    prodImpl: CrossSellHomeNotificationServiceImpl,
    demoImpl: DemoCrossSellHomeNotificationService,
  ): Provider<CrossSellHomeNotificationService> = CrossSellHomeNotificationServiceProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
