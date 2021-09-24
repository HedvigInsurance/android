package com.hedvig.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.app.feature.chat.data.ChatEventDataStore
import com.hedvig.app.feature.chat.data.ChatEventStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ChatEventStoreModule {

    @Provides
    fun provideChatEventStore(
        dataStore: DataStore<Preferences>
    ): ChatEventStore {
        return ChatEventDataStore(dataStore)
    }
}
