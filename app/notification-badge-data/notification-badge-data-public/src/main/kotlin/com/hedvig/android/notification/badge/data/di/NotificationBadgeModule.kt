package com.hedvig.android.notification.badge.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCase
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellRecommendationIdUseCaseImpl
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceImpl
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceProvider
import com.hedvig.android.notification.badge.data.crosssell.home.DemoCrossSellHomeNotificationService
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import org.koin.dsl.module

val notificationBadgeModule = module {
  single<CrossSellHomeNotificationServiceProvider> {
    CrossSellHomeNotificationServiceProvider(
      demoManager = get<DemoManager>(),
      demoImpl = DemoCrossSellHomeNotificationService(),
      prodImpl = get<CrossSellHomeNotificationServiceImpl>(),
    )
  }
  single<CrossSellNotificationBadgeService> {
    CrossSellNotificationBadgeService(get<GetCrossSellRecommendationIdUseCase>(), get<NotificationBadgeStorage>())
  }
  single<GetCrossSellRecommendationIdUseCase> {
    GetCrossSellRecommendationIdUseCaseImpl(get<ApolloClient>())
  }
  single<NotificationBadgeStorage> { DatastoreNotificationBadgeStorage(get()) }

  single<CrossSellHomeNotificationServiceImpl> {
    CrossSellHomeNotificationServiceImpl(get<CrossSellNotificationBadgeService>(), get<DataStore<Preferences>>())
  }
}
