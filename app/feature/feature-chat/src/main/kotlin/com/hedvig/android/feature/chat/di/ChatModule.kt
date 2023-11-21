package com.hedvig.android.feature.chat.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.ChatRepository
import com.hedvig.android.feature.chat.ChatRepositoryImpl
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.FileService
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventDataStore
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
  single<ChatClosedEventStore> { ChatClosedEventDataStore(get()) }
  viewModel<ChatViewModel> {
    ChatViewModel(
      // todo fake provider
      Provider { get<ChatRepository>() },
      get<ChatClosedEventStore>(),
      get<FeatureManager>(),
      get<DemoManager>(),
    )
  }
  single<ChatRepository> { ChatRepositoryImpl(get<ApolloClient>()) }

  single<FileService> { FileService(get()) }
}
