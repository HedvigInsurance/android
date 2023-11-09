@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.datastoreFileQualifier
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdDataStoreImpl
import java.io.File
import org.koin.dsl.module

private const val preferencesDatastoreFileName = "hedvig_data_store_preferences"

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore-preferences/src/main/java/androidx/datastore/preferences/PreferenceDataStoreFile.kt;l=37-38
private const val preferencesDatastoreFileExtension = ".preferences_pb"

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
private const val preferencesDataStoreFileDirectory = "datastore"

val dataStoreModule = module {
  single<DataStore<Preferences>> {
    PreferenceDataStoreFactory.create(
      produceFile = {
        val fileNameWithDatastoreExtension = "$preferencesDatastoreFileName$preferencesDatastoreFileExtension"
        val datastoreFile = get<File>(datastoreFileQualifier)
        File(datastoreFile, "$preferencesDataStoreFileDirectory/$fileNameWithDatastoreExtension")
      },
    )
  }
  single<DeviceIdDataStore> { DeviceIdDataStoreImpl(get(), get<ApplicationScope>()) }
}
