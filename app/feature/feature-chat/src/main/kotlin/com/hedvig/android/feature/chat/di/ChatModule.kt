package com.hedvig.android.feature.chat.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.feature.chat.data.ChatRepositoryDemo
import com.hedvig.android.feature.chat.data.ChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetChatRepositoryProvider
import com.hedvig.android.feature.chat.floating.ChatTooltipStorage
import com.hedvig.android.feature.chat.floating.FloatingBubbleViewModel
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
    val retrofit = Retrofit.Builder()
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

  single<ChatTooltipStorage> {
    ChatTooltipStorage(get<DataStore<Preferences>>())
  }
  viewModel<FloatingBubbleViewModel> {
    FloatingBubbleViewModel(get<ChatTooltipStorage>(), get<Clock>())
  }
}
