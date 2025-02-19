package com.hedvig.android.feature.chat.di

import android.content.Context
import androidx.room.RoomDatabase
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.fileupload.FileService
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
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val chatModule = module {
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
      fileService = get<FileService>(),
      botServiceService = get<BotServiceService>(),
      clock = get<Clock>(),
      context = get<Context>(),
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
      clock = get<Clock>(),
    )
  }
}
