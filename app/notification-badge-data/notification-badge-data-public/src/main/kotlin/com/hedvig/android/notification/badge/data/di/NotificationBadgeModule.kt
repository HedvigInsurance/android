package com.hedvig.android.notification.badge.data.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellCardNotificationBadgeServiceProvider
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCaseImpl
import com.hedvig.android.notification.badge.data.crosssell.bottomnav.CrossSellBottomNavNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeServiceImpl
import com.hedvig.android.notification.badge.data.crosssell.card.DemoCrossSellCardNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceImpl
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceProvider
import com.hedvig.android.notification.badge.data.crosssell.home.DemoCrossSellHomeNotificationService
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import org.koin.dsl.module

val notificationBadgeModule = module {
  single<CrossSellHomeNotificationServiceProvider> {
    CrossSellHomeNotificationServiceProvider(
      demoManager = get<DemoManager>(),
      demoImpl = DemoCrossSellHomeNotificationService(),
      prodImpl = get<CrossSellHomeNotificationServiceImpl>(),
    )
  }
  single<CrossSellBottomNavNotificationBadgeService> { CrossSellBottomNavNotificationBadgeService(get()) }
  single<CrossSellNotificationBadgeService> { CrossSellNotificationBadgeService(get(), get()) }
  single<GetCrossSellRecommendationIdUseCase> {
    GetCrossSellRecommendationIdUseCaseImpl(get<ApolloClient>())
  }
  single<NotificationBadgeStorage> { DatastoreNotificationBadgeStorage(get()) }
  single<ReferralsNotificationBadgeService> { ReferralsNotificationBadgeService(get()) }
  single<TabNotificationBadgeService> { TabNotificationBadgeService(get(), get()) }

  single<CrossSellCardNotificationBadgeServiceProvider> {
    CrossSellCardNotificationBadgeServiceProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<DemoCrossSellCardNotificationBadgeService>(),
      prodImpl = get<CrossSellCardNotificationBadgeServiceImpl>(),
    )
  }
  single<CrossSellCardNotificationBadgeServiceImpl> {
    CrossSellCardNotificationBadgeServiceImpl(get())
  }
  single<DemoCrossSellCardNotificationBadgeService> {
    DemoCrossSellCardNotificationBadgeService()
  }
  single<CrossSellHomeNotificationServiceImpl>{
    CrossSellHomeNotificationServiceImpl(get(), get())
  }

}
