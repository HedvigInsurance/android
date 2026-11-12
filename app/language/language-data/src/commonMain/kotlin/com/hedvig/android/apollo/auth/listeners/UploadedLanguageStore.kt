package com.hedvig.android.apollo.auth.listeners

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.Language
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * The language last sent to the backend for the logged-in member, so it is only sent again when it changed.
 */
interface UploadedLanguageStore {
  suspend fun lastUploadedLanguage(): Language?

  suspend fun setLastUploadedLanguage(language: Language)
}

@ContributesBinding(AppScope::class)
@Inject
internal class DataStoreUploadedLanguageStore(
  private val dataStore: DataStore<Preferences>,
) : UploadedLanguageStore {
  override suspend fun lastUploadedLanguage(): Language? {
    val languageTag = dataStore.data.first()[lastUploadedLanguageKey] ?: return null
    return Language.entries.firstOrNull { it.toBcp47Format() == languageTag }
  }

  override suspend fun setLastUploadedLanguage(language: Language) {
    dataStore.edit { it[lastUploadedLanguageKey] = language.toBcp47Format() }
  }

  private val lastUploadedLanguageKey = stringPreferencesKey("last_uploaded_member_language")
}
