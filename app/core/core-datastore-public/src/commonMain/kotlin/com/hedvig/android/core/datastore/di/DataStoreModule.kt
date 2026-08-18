@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

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
