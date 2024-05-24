package com.hedvig.android.core.datastore

import app.cash.turbine.Turbine
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeSettingsDataStore() : SettingsDataStore {
  val themeTurbine = Turbine<Theme>()
  val subscriptionPreferenceTurbine = Turbine<Boolean>()

  override suspend fun setTheme(theme: Theme) {
    themeTurbine.add(theme)
  }

  override fun observeTheme(): Flow<Theme> {
    return themeTurbine.asChannel().receiveAsFlow()
  }

  override suspend fun setSubscriptionPreference(subscribe: Boolean) {
    subscriptionPreferenceTurbine.add(subscribe)
  }

  override fun observeSubscriptionPreference(): Flow<Boolean> {
    return subscriptionPreferenceTurbine.asChannel().receiveAsFlow()
  }
}
