package com.hedvig.android.data.chat.icon.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.data.chat.icon.GetChatIconAppStateUseCase
import com.hedvig.android.data.chat.icon.GetChatIconAppStateUseCaseImpl
import com.hedvig.android.data.chat.icon.ShouldShowChatIconUseCase
import com.hedvig.android.data.chat.icon.ShouldShowChatIconUseCaseImpl
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val dataChatIconModule = module {
  single<ShouldShowChatIconUseCase> {
    ShouldShowChatIconUseCaseImpl(get<ApolloClient>(), get<FeatureManager>())
  }
  single<GetChatIconAppStateUseCase> {
    GetChatIconAppStateUseCaseImpl(
      get<SettingsDataStore>(),
      get<FeatureManager>(),
      get<ShouldShowChatIconUseCase>(),
      get<ChatLastMessageReadRepository>(),
    )
  }
}
