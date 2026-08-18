package com.hedvig.android.core.datastore

import app.cash.turbine.Turbine
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeSettingsDataStore() : SettingsDataStore {
  val themeTurbine = Turbine<Theme>()
  val subscriptionPreferenceTurbine = Turbine<Boolean>()
  val analyticsConsentTurbine = Turbine<AnalyticsConsent>()

  override suspend fun setTheme(theme: Theme) {
    themeTurbine.add(theme)
  }

  override fun observeTheme(): Flow<Theme> {
    return themeTurbine.asChannel().receiveAsFlow()
  }

  override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) {
    subscriptionPreferenceTurbine.add(subscribe)
  }

  override fun observeEmailSubscriptionPreference(): Flow<Boolean> {
    return subscriptionPreferenceTurbine.asChannel().receiveAsFlow()
  }

  override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
    analyticsConsentTurbine.add(consent)
  }

  override fun observeAnalyticsConsent(): Flow<AnalyticsConsent> {
    return analyticsConsentTurbine.asChannel().receiveAsFlow()
  }
}
