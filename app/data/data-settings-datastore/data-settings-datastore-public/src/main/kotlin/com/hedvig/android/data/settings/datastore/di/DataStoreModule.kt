package com.hedvig.android.data.settings.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.data.settings.datastore.SettingsDataStoreImpl
import org.koin.dsl.module

val settingsDatastoreModule = module {
  single<SettingsDataStore> { SettingsDataStoreImpl(get<DataStore<Preferences>>()) }
}
