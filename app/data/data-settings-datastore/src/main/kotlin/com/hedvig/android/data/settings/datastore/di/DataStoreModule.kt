package com.hedvig.android.data.settings.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import org.koin.dsl.module

val settingsDatastoreModule = module {
  single<SettingsDataStore> { SettingsDataStore(get<DataStore<Preferences>>()) }
}
