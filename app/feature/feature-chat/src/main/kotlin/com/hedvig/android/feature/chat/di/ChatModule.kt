package com.hedvig.android.feature.chat.di

import com.hedvig.android.feature.chat.ChatEventDataStore
import com.hedvig.android.feature.chat.ChatEventStore
import com.hedvig.android.feature.chat.FileService
import org.koin.dsl.module

val chatModule = module {
  single<ChatEventStore> { ChatEventDataStore(get()) }

  single<FileService> { FileService(get()) }
}
