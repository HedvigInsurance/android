package com.hedvig.android.feature.chat.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.feature.chat.ChatEventDataStore
import com.hedvig.android.feature.chat.ChatEventStore
import com.hedvig.android.feature.chat.ChatRepository
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.FileService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
  single<ChatEventStore> { ChatEventDataStore(get()) }
  single<ChatRepository> { ChatRepository(get<ApolloClient>(giraffeClient), get(), get()) }
  viewModel<ChatViewModel> { ChatViewModel(get(), get(), get(), get()) }

  single<FileService> { FileService(get()) }
}
