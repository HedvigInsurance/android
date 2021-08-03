package com.hedvig.app.feature.chat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface ChatEventStore {
    suspend fun increaseChatClosedCounter()
    fun observeChatClosedCounter(): Flow<Int>
}

class ChatEventDataStore(
    private val dataStore: DataStore<Preferences>
) : ChatEventStore {

    override suspend fun increaseChatClosedCounter() {
        dataStore.edit {
            it[CHAT_CLOSED_COUNTER] = (it[CHAT_CLOSED_COUNTER] ?: 0) + 1
        }
    }

    override fun observeChatClosedCounter() = dataStore.data
        .catch { e(it) }
        .map { it[CHAT_CLOSED_COUNTER] ?: 0 }

    companion object {
        private val CHAT_CLOSED_COUNTER = intPreferencesKey("CHAT_CLOSED_COUNTER")
    }
}
