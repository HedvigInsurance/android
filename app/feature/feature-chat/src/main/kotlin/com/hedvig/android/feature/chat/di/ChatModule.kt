package com.hedvig.android.feature.chat.di

import android.content.Context
import arrow.retrofit.adapter.either.EitherCallAdapterFactory
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.android.FileService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventDataStore
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.ChatRepositoryDemo
import com.hedvig.android.feature.chat.data.ChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetChatRepositoryProvider
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val chatModule = module {
  single<ChatClosedEventStore> { ChatClosedEventDataStore(get()) }
  viewModel<ChatViewModel> {
    ChatViewModel(
      chatRepository = get<GetChatRepositoryProvider>(),
      featureManager = get<FeatureManager>(),
      clock = get<Clock>(),
    )
  }
  single<ChatRepositoryImpl> {
    ChatRepositoryImpl(
      apolloClient = get<ApolloClient>(),
      botServiceService = get<BotServiceService>(),
      fileService = get<FileService>(),
      contentResolver = get<Context>().contentResolver,
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

  single<FileService> { FileService(get<Context>().contentResolver) }

  single<BotServiceService> {
    val retrofit = Retrofit.Builder()
      .callFactory(get<OkHttpClient>())
      .baseUrl("${get<HedvigBuildConstants>().urlBotService}/api/")
      .addCallAdapterFactory(EitherCallAdapterFactory.create())
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()
    retrofit.create(BotServiceService::class.java)
  }
}
