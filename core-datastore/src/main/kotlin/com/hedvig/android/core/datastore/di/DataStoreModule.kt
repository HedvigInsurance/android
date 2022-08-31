@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdDataStoreImpl
import org.koin.dsl.module

val dataStoreModule = module {
  single<DataStore<Preferences>> {
    PreferenceDataStoreFactory.create(
      produceFile = {
        get<Context>().preferencesDataStoreFile("hedvig_data_store_preferences")
      },
    )
  }
}

val deviceIdDataStoreModule = module {
  single<DeviceIdDataStore> { DeviceIdDataStoreImpl(get()) }
}
