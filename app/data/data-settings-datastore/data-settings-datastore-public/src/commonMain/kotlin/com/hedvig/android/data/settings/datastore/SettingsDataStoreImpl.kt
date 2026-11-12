package com.hedvig.android.data.settings.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.theme.Theme
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsDataStore {
  suspend fun setTheme(theme: Theme)

  /**
   * Returns if a specific [Theme] was explicitly chosen, otherwise null.
   */
  fun observeTheme(): Flow<Theme?>

  suspend fun setEmailSubscriptionPreference(subscribe: Boolean)

  fun observeEmailSubscriptionPreference(): Flow<Boolean>

  suspend fun setAnalyticsConsent(consent: AnalyticsConsent)

  /**
   * The member's product analytics consent decision. [AnalyticsConsent.NOT_DECIDED] when they
   * have not made an explicit choice yet.
   */
  fun observeAnalyticsConsent(): Flow<AnalyticsConsent>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SettingsDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : SettingsDataStore {
  override suspend fun setTheme(theme: Theme) {
    dataStore.edit {
      it[themeKey] = theme.name
    }
  }

  override fun observeTheme(): Flow<Theme?> {
    return dataStore.data.map {
      it[themeKey]?.let { themeString -> Theme.valueOf(themeString) }
    }
  }

  override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) {
    dataStore.edit {
      it[subscriptionKey] = subscribe
    }
  }

  override fun observeEmailSubscriptionPreference(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
      // here we assume that member is subscribed by default in customer.io
      preferences[subscriptionKey] ?: true
    }
  }

  override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
    dataStore.edit {
      it[analyticsConsentKey] = consent.name
    }
  }

  override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> {
    return dataStore.data.map { preferences ->
      preferences[analyticsConsentKey]
        ?.let { stored -> AnalyticsConsent.entries.firstOrNull { it.name == stored } }
        ?: AnalyticsConsent.NOT_DECIDED
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
    private val subscriptionKey = booleanPreferencesKey(
      "com.hedvig.android.data.settings.datastore.settings-email-subscription",
    )
    private val analyticsConsentKey = stringPreferencesKey(
      "com.hedvig.android.data.settings.datastore.settings-analytics-consent",
    )
  }
}
