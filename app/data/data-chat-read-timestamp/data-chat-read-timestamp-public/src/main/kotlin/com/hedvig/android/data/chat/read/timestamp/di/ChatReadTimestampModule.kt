package com.hedvig.android.data.chat.read.timestamp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepositoryImpl
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageSeenClearingAuthEventListener
import com.hedvig.android.data.chat.read.timestamp.ChatMessageTimestampStorage
import com.hedvig.android.data.chat.read.timestamp.ChatMessageTimestampStorageImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val chatReadTimestampModule = module {
  single<ChatMessageTimestampStorage> { ChatMessageTimestampStorageImpl(get<DataStore<Preferences>>()) }
  single<ChatLastMessageReadRepository> {
    ChatLastMessageReadRepositoryImpl(get<ChatMessageTimestampStorage>(), get<ApolloClient>())
  }
  single<ChatLastMessageSeenClearingAuthEventListener> {
    ChatLastMessageSeenClearingAuthEventListener(get<ChatMessageTimestampStorage>())
  } bind AuthEventListener::class
}
