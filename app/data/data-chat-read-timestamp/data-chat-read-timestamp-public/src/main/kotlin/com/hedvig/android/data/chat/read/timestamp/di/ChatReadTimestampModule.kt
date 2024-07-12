package com.hedvig.android.data.chat.read.timestamp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepositoryImpl
import com.hedvig.android.data.chat.read.timestamp.ChatMessageTimestampStorage
import com.hedvig.android.data.chat.read.timestamp.ChatMessageTimestampStorageImpl
import com.hedvig.android.featureflags.FeatureManager
import org.koin.dsl.module

val chatReadTimestampModule = module {
  single<ChatMessageTimestampStorage> { ChatMessageTimestampStorageImpl(get<DataStore<Preferences>>()) }
  single<ChatLastMessageReadRepository> {
    ChatLastMessageReadRepositoryImpl(get<ChatMessageTimestampStorage>(), get<ApolloClient>(), get<FeatureManager>())
  }
}
