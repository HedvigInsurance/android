package com.hedvig.android.notification.firebase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal interface FCMTokenStorage {
  fun getToken(): Flow<String?>

  suspend fun saveToken(token: String)

  suspend fun clearToken()
}

internal class DatastoreFCMTokenStorage(
  private val datastore: DataStore<Preferences>,
) : FCMTokenStorage {
  override fun getToken(): Flow<String?> {
    return datastore.data.map { preferences: Preferences ->
      preferences[FIREBASE_TOKEN_KEY]
    }
  }

  override suspend fun saveToken(token: String) {
    datastore.edit { preferences ->
      preferences[FIREBASE_TOKEN_KEY] = token
    }
  }

  override suspend fun clearToken() {
    datastore.edit { preferences ->
      preferences.remove(FIREBASE_TOKEN_KEY)
    }
  }

  companion object {
    private val FIREBASE_TOKEN_KEY = stringPreferencesKey("FIREBASE_TOKEN_KEY")
  }
}
