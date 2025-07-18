package com.hedvig.android.feature.home.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorageImpl
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationServiceProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  single<GetHomeDataUseCaseImpl> {
    GetHomeDataUseCaseImpl(
      get<ApolloClient>(),
      get<HasAnyActiveConversationUseCase>(),
      get<GetMemberRemindersUseCase>(),
      get<FeatureManager>(),
      get<Clock>(),
      get<TimeZone>(),
      get<GetTravelAddonBannerInfoUseCaseProvider>(),
    )
  }
  single<SeenImportantMessagesStorage> {
    SeenImportantMessagesStorageImpl()
  }
  single<GetHomeDataUseCaseDemo> {
    GetHomeDataUseCaseDemo()
  }
  single {
    GetHomeDataUseCaseProvider(
      demoManager = get<DemoManager>(),
      prodImpl = get<GetHomeDataUseCaseImpl>(),
      demoImpl = get<GetHomeDataUseCaseDemo>(),
    )
  }
  viewModel<HomeViewModel> {
    HomeViewModel(
      get<GetHomeDataUseCaseProvider>(),
      get<SeenImportantMessagesStorage>(),
      get<CrossSellHomeNotificationServiceProvider>(),
      get<ApplicationScope>(),
    )
  }
}
