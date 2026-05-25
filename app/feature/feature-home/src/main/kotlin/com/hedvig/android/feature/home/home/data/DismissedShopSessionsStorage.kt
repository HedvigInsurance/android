package com.hedvig.android.feature.home.home.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.ktor.utils.io.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

interface DismissedShopSessionsStorage {
  fun observeDismissedSessionIds(): Flow<Set<String>>

  suspend fun clear()

  suspend fun dismiss(sessionId: String)
}

internal class DismissedShopSessionsStorageImpl(
  private val dataStore: DataStore<Preferences>,
) : DismissedShopSessionsStorage {
  override fun observeDismissedSessionIds(): Flow<Set<String>> = dataStore.data.map {
    it[KEY]?.let(::decode)?.keys.orEmpty()
  }

  override suspend fun clear() {
    dataStore.edit { prefs ->
      prefs[KEY] = encode(emptyMap())
    }
  }

  override suspend fun dismiss(sessionId: String) {
    val dismissedAtMillis = Clock.System.now().toEpochMilliseconds()
    dataStore.edit { prefs ->
      val current = prefs[KEY]?.let(::decode) ?: emptyMap()
      prefs[KEY] = encode(current + (sessionId to dismissedAtMillis))
    }
  }

  private fun encode(map: Map<String, Long>): String = Json.encodeToString(SERIALIZER, map)

  private fun decode(raw: String): Map<String, Long>? = try {
    Json.decodeFromString(SERIALIZER, raw)
  } catch (e: Throwable) {
    if (e is CancellationException) throw e
    logcat(LogPriority.ERROR, e) { "Decoding dismissed shop sessions failed; treating store as empty" }
    null
  }

  companion object {
    private val KEY = stringPreferencesKey("com.hedvig.android.feature.home.dismissed_ongoing_shop_sessions")
    private val SERIALIZER = MapSerializer(String.serializer(), Long.serializer())
  }
}
