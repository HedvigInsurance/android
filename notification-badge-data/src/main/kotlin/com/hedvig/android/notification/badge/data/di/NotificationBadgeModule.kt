package com.hedvig.android.notification.badge.data.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCaseImpl
import com.hedvig.android.notification.badge.data.crosssell.bottomnav.CrossSellBottomNavNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val notificationBadgeModule = module {
  single<CrossSellBottomNavNotificationBadgeService> { CrossSellBottomNavNotificationBadgeService(get()) }
  single<CrossSellCardNotificationBadgeService> { CrossSellCardNotificationBadgeService(get()) }
  single<CrossSellNotificationBadgeService> { CrossSellNotificationBadgeService(get(), get()) }
  single<GetCrossSellsContractTypesUseCase> {
    GetCrossSellsContractTypesUseCaseImpl(get<ApolloClient>(giraffeClient), get())
  }
  single<NotificationBadgeStorage> { DatastoreNotificationBadgeStorage(get()) }
  single<ReferralsNotificationBadgeService> { ReferralsNotificationBadgeService(get(), get()) }
  single<TabNotificationBadgeService> { TabNotificationBadgeService(get(), get()) }
}
