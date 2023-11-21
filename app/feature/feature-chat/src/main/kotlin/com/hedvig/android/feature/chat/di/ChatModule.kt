package com.hedvig.android.feature.chat.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.ChatRepository
import com.hedvig.android.feature.chat.ChatRepositoryNew
import com.hedvig.android.feature.chat.ChatRepositoryNewImpl
import com.hedvig.android.feature.chat.ChatViewModelNew
import com.hedvig.android.feature.chat.FileService
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventDataStore
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
  single<ChatClosedEventStore> { ChatClosedEventDataStore(get()) }
  single<ChatRepository> { ChatRepository(get<ApolloClient>(giraffeClient), get(), get()) }

  viewModel<ChatViewModelNew> {
    ChatViewModelNew(
      // todo fake provider
      Provider { get<ChatRepositoryNew>() },
      get<ChatClosedEventStore>(),
      get<FeatureManager>(),
      get<DemoManager>(),
    )
  }
  single<ChatRepositoryNew> { ChatRepositoryNewImpl(get<ApolloClient>(octopusClient)) }

  single<FileService> { FileService(get()) }
}
