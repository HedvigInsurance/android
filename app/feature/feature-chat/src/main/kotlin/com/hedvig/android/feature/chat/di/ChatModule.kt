package com.hedvig.android.feature.chat.di

import android.content.Context
import androidx.room.Room
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.cbm.CbmChatRepository
import com.hedvig.android.feature.chat.cbm.CbmChatViewModel
import com.hedvig.android.feature.chat.cbm.data.GetAllConversationsUseCase
import com.hedvig.android.feature.chat.cbm.data.GetAllConversationsUseCaseImpl
import com.hedvig.android.feature.chat.cbm.database.AppDatabase
import com.hedvig.android.feature.chat.cbm.database.ChatDao
import com.hedvig.android.feature.chat.cbm.database.ConversationDao
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.inbox.InboxViewModel
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.feature.chat.data.ChatRepositoryDemo
import com.hedvig.android.feature.chat.data.ChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetChatRepositoryProvider
import com.hedvig.android.navigation.core.AppDestination
import kotlin.coroutines.CoroutineContext
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

  /**
   * [com.hedvig.app.feature.chat.service.ReplyWorker] also needs an instance of ChatRepository itself, without
   * necessarily caring about demo mode or not. If there is a notification arriving, even if they are in demo mode
   * somehow, the real chat repository should be used.
   */
  single<ChatRepository> {
    get<ChatRepositoryImpl>()
  }

  // cbm
  single<GetAllConversationsUseCase> {
    GetAllConversationsUseCaseImpl(get<ApolloClient>())
  }

  single<AppDatabase> {
    val applicationContext = get<Context>()
    val dbFile = applicationContext.getDatabasePath("hedvig_chat_database.db")
    Room
      .databaseBuilder<AppDatabase>(applicationContext, dbFile.absolutePath)
      .fallbackToDestructiveMigration(true)
      .setQueryCoroutineContext(get<CoroutineContext>(ioDispatcherQualifier))
      .build()
  }
  single<ChatDao> {
    get<AppDatabase>().chatDao()
  }
  single<RemoteKeyDao> {
    get<AppDatabase>().remoteKeyDao()
  }
  single<ConversationDao> {
    get<AppDatabase>().conversationDao()
  }

  viewModel<InboxViewModel> {
    InboxViewModel(get<GetAllConversationsUseCase>())
  }

  single<CbmChatRepository> {
    CbmChatRepository(
      apolloClient = get<ApolloClient>(),
      database = get<AppDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      fileService = get<FileService>(),
      botServiceService = get<BotServiceService>(),
      clock = get<Clock>(),
    )
  }

  viewModel<CbmChatViewModel> { (conversationId: String) ->
    CbmChatViewModel(
      conversationId = conversationId,
      database = get<AppDatabase>(),
      chatDao = get<ChatDao>(),
      remoteKeyDao = get<RemoteKeyDao>(),
      conversationDao = get<ConversationDao>(),
      chatRepository = get<CbmChatRepository>(),
      clock = get<Clock>(),
    )
  }
}
