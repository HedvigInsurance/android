package com.hedvig.android.data.chat.read.timestamp.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepositoryImpl
import org.koin.dsl.module

val chatReadTimestampModule = module {
  single<ChatLastMessageReadRepository> {
    ChatLastMessageReadRepositoryImpl(
      get<ApolloClient>(),
      get<ConversationDao>(),
    )
  }
}
