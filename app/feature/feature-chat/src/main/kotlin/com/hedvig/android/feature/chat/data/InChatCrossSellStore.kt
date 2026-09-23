package com.hedvig.android.feature.chat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Remembers, per install, how the in-chat cross-sell card has been used in each conversation. A
 * conversation is offered the card at most once, and once the member turns it down it stays gone for
 * that conversation.
 */
internal interface InChatCrossSellStore {
  fun observeDismissedConversationIds(): Flow<Set<String>>

  suspend fun dismiss(conversationId: String)

  /**
   * Records that the card was shown, returning true only the first time for a conversation, so that
   * the caller reports one prompt per conversation however often the screen is re-entered.
   */
  suspend fun markPrompted(conversationId: String): Boolean
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class InChatCrossSellStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : InChatCrossSellStore {
  override fun observeDismissedConversationIds(): Flow<Set<String>> = dataStore.data
    .map { it[DISMISSED_KEY].orEmpty() }
    .catch { error ->
      // The chat must still render if this local-only preference can't be read.
      logcat(LogPriority.ERROR, error) { "Reading dismissed in-chat cross-sells failed; treating none as dismissed" }
      emit(emptySet())
    }

  override suspend fun dismiss(conversationId: String) {
    runCatching {
      dataStore.edit { preferences ->
        preferences[DISMISSED_KEY] = preferences[DISMISSED_KEY].orEmpty() + conversationId
      }
    }.onFailure { error ->
      logcat(LogPriority.ERROR, error) { "Persisting in-chat cross-sell dismissal failed for $conversationId" }
    }
  }

  override suspend fun markPrompted(conversationId: String): Boolean {
    var isFirstPrompt = false
    runCatching {
      dataStore.edit { preferences ->
        val alreadyPrompted = preferences[PROMPTED_KEY].orEmpty()
        isFirstPrompt = conversationId !in alreadyPrompted
        if (isFirstPrompt) {
          preferences[PROMPTED_KEY] = alreadyPrompted + conversationId
        }
      }
    }.onFailure { error ->
      logcat(LogPriority.ERROR, error) { "Persisting in-chat cross-sell prompt failed for $conversationId" }
      isFirstPrompt = false
    }
    return isFirstPrompt
  }

  companion object {
    internal const val DISMISSED_KEY_NAME = "com.hedvig.android.feature.chat.in_chat_cross_sell_dismissed"
    internal const val PROMPTED_KEY_NAME = "com.hedvig.android.feature.chat.in_chat_cross_sell_prompted"
    private val DISMISSED_KEY = stringSetPreferencesKey(DISMISSED_KEY_NAME)
    private val PROMPTED_KEY = stringSetPreferencesKey(PROMPTED_KEY_NAME)
  }
}
