package com.hedvig.android.notification.badge.data.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellCardNotificationBadgeServiceProvider
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellIdentifiersUseCase
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellIdentifiersUseCaseImpl
import com.hedvig.android.notification.badge.data.crosssell.bottomnav.CrossSellBottomNavNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeServiceImpl
import com.hedvig.android.notification.badge.data.crosssell.card.DemoCrossSellCardNotificationBadgeService
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val notificationBadgeModule = module {
  single<CrossSellBottomNavNotificationBadgeService> { CrossSellBottomNavNotificationBadgeService(get()) }
  single<CrossSellNotificationBadgeService> { CrossSellNotificationBadgeService(get(), get()) }
  single<GetCrossSellIdentifiersUseCase> {
    GetCrossSellIdentifiersUseCaseImpl(get<ApolloClient>())
  }
  single<NotificationBadgeStorage> { DatastoreNotificationBadgeStorage(get()) }
  single<ReferralsNotificationBadgeService> { ReferralsNotificationBadgeService(get()) }
  single<TabNotificationBadgeService> { TabNotificationBadgeService(get(), get()) }

  single<CrossSellCardNotificationBadgeServiceProvider> {
    CrossSellCardNotificationBadgeServiceProvider(
      get<DemoManager>(),
      get<CrossSellCardNotificationBadgeServiceImpl>(),
      get<DemoCrossSellCardNotificationBadgeService>(),
    )
  }
  single<CrossSellCardNotificationBadgeServiceImpl> {
    CrossSellCardNotificationBadgeServiceImpl(get())
  }
  single<DemoCrossSellCardNotificationBadgeService> {
    DemoCrossSellCardNotificationBadgeService()
  }
}
