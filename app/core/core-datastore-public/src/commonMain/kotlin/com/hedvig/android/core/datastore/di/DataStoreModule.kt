@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdDataStoreImpl
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

val dataStoreModule = module {
  includes(platformDataStoreModule)
  single<DeviceIdDataStore> { DeviceIdDataStoreImpl(get<DataStore<Preferences>>(), get<ApplicationScope>()) }
}

internal expect val platformDataStoreModule: Module

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> {
  return PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
}

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/androidMain/kotlin/androidx/datastore/DataStoreFile.android.kt;l=35
private const val preferencesDataStoreFileDirectory = "datastore"
private const val preferencesDatastoreFileName = "hedvig_data_store_preferences"

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore-preferences/src/androidMain/kotlin/androidx/datastore/preferences/PreferenceDataStoreFile.android.kt;l=37
private const val preferencesDatastoreFileExtension = ".preferences_pb"

internal const val dataStoreFileName =
  "$preferencesDataStoreFileDirectory/$preferencesDatastoreFileName$preferencesDatastoreFileExtension"
