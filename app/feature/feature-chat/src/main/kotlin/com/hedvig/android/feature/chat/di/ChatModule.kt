package com.hedvig.android.feature.chat.di

import android.content.Context
import androidx.room.RoomDatabase
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.fileupload.FileUploadService
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.feature.chat.CbmChatViewModel
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.CbmChatRepositoryDemo
import com.hedvig.android.feature.chat.data.CbmChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetAllConversationsUseCase
import com.hedvig.android.feature.chat.data.GetAllConversationsUseCaseImpl
import com.hedvig.android.feature.chat.data.GetCbmChatRepositoryProvider
import com.hedvig.android.feature.chat.inbox.InboxViewModel
import com.hedvig.android.featureflags.FeatureManager
import io.ktor.client.HttpClient
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
  single<BotServiceService> {
    BotServiceService(
      fileUploadService = get<FileUploadService>(),
      buildConstants = get<HedvigBuildConstants>(),
    )
  }

  single<GetAllConversationsUseCase> {
    GetAllConversationsUseCaseImpl(get<ApolloClient>())
  }

  viewModel<InboxViewModel> {
    InboxViewModel(get<GetAllConversationsUseCase>())
  }

  single<CbmChatRepositoryImpl> {
    CbmChatRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      database = get<RoomDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      botServiceService = get<BotServiceService>(),
      contentResolver = get<Context>().contentResolver,
      clock = get<Clock>(),
    )
  }
  single<CbmChatRepositoryDemo> {
    CbmChatRepositoryDemo(get<Clock>())
  }

  single<GetCbmChatRepositoryProvider> {
    GetCbmChatRepositoryProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<CbmChatRepositoryDemo>(),
      prodImpl = get<CbmChatRepositoryImpl>(),
    )
  }

  viewModel<CbmChatViewModel> { (conversationId: String) ->
    CbmChatViewModel(
      conversationId = conversationId,
      database = get<RoomDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      chatRepository = get<GetCbmChatRepositoryProvider>(),
      featureManager = get<FeatureManager>(),
      clock = get<Clock>(),
      context = get<Context>(),
    )
  }
}
