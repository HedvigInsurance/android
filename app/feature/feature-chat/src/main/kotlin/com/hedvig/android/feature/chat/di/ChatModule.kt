package com.hedvig.android.feature.chat.di

import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.cbm.CbmChatRepository
import com.hedvig.android.feature.chat.cbm.CbmChatRepositoryImpl
import com.hedvig.android.feature.chat.cbm.CbmChatViewModel
import com.hedvig.android.feature.chat.cbm.data.CbmChatRepositoryDemo
import com.hedvig.android.feature.chat.cbm.data.GetAllConversationsUseCase
import com.hedvig.android.feature.chat.cbm.data.GetAllConversationsUseCaseImpl
import com.hedvig.android.feature.chat.cbm.data.GetCbmChatRepositoryProvider
import com.hedvig.android.feature.chat.cbm.inbox.InboxViewModel
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.feature.chat.data.ChatRepositoryDemo
import com.hedvig.android.feature.chat.data.ChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetChatRepositoryProvider
import com.hedvig.android.navigation.core.AppDestination
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val chatModule = module {
  viewModel<ChatViewModel> { parametersHolder ->
    val chatContext = parametersHolder.getOrNull<AppDestination.Chat.ChatContext>()
    ChatViewModel(
      chatRepository = get<GetChatRepositoryProvider>(),
      clock = get<Clock>(),
      chatContext = chatContext,
    )
  }
  single<ChatRepositoryImpl> {
    ChatRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      botServiceService = get<BotServiceService>(),
      fileService = get<FileService>(),
      chatLastMessageReadRepository = get<ChatLastMessageReadRepository>(),
    )
  }
  single<ChatRepositoryDemo> {
    ChatRepositoryDemo(get<Clock>())
  }
  single<GetChatRepositoryProvider> {
    GetChatRepositoryProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<ChatRepositoryDemo>(),
      prodImpl = get<ChatRepositoryImpl>(),
    )
  }

  single<BotServiceService> {
    val retrofit = Retrofit
      .Builder()
      .callFactory(get<OkHttpClient>())
      .baseUrl("${get<HedvigBuildConstants>().urlBotService}/api/")
      .addCallAdapterFactory(EitherCallAdapterFactory.create())
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()
    retrofit.create(BotServiceService::class.java)
  }

  // cbm
  single<GetAllConversationsUseCase> {
    GetAllConversationsUseCaseImpl(get<ApolloClient>(), get<ConversationDao>())
  }

  viewModel<InboxViewModel> {
    InboxViewModel(get<GetAllConversationsUseCase>())
  }

  single<CbmChatRepositoryImpl> {
    CbmChatRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      database = get<AppDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      conversationDao = get<ConversationDao>(),
      fileService = get<FileService>(),
      botServiceService = get<BotServiceService>(),
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
      database = get<AppDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      conversationDao = get<ConversationDao>(),
      chatRepository = get<GetCbmChatRepositoryProvider>(),
      clock = get<Clock>(),
    )
  }
}
