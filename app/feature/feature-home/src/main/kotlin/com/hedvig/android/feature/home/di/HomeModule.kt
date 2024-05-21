package com.hedvig.android.feature.home.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.chat.icon.GetChatIconAppStateUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorageImpl
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.notification.badge.data.crosssell.CrossSellCardNotificationBadgeServiceProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  single<GetHomeDataUseCaseImpl> {
    GetHomeDataUseCaseImpl(
      get<ApolloClient>(),
      get<GetMemberRemindersUseCase>(),
      get<GetChatIconAppStateUseCase>(),
      get<FeatureManager>(),
      get<Clock>(),
      get<TimeZone>(),
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
      get<CrossSellCardNotificationBadgeServiceProvider>(),
      get<ApplicationScope>(),
    )
  }
}
