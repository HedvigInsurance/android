package com.hedvig.android.notification.badge.data.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.notification.badge.data.payment.DemoMissedPaymentNotificationService
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationServiceProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface NotificationBadgeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideMissedPaymentNotificationServiceProvider(
    demoManager: DemoManager,
    prodImpl: MissedPaymentNotificationService,
    demoImpl: DemoMissedPaymentNotificationService,
  ): MissedPaymentNotificationServiceProvider = MissedPaymentNotificationServiceProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
