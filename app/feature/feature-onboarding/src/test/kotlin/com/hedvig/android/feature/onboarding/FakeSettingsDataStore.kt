package com.hedvig.android.feature.onboarding

import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSettingsDataStore : SettingsDataStore {
  val consent = MutableStateFlow(AnalyticsConsent.NOT_DECIDED)
  val theme = MutableStateFlow<Theme?>(null)

  override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
    this.consent.value = consent
  }

  override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> = consent

  override suspend fun setTheme(theme: Theme) {
    this.theme.value = theme
  }

  override fun observeTheme(): Flow<Theme?> = theme

  override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) = error("unused")

  override fun observeEmailSubscriptionPreference(): Flow<Boolean> = error("unused")
}
