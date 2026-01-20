package com.hedvig.android.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.datastore.AndroidDeviceIdFetcher
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdFetcher
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataStoreModule: Module = module {
  // https://developer.android.com/kotlin/multiplatform/datastore#creating-datastore
  single<DataStore<Preferences>> {
    val context = get<Context>()
    createDataStore { context.applicationContext.filesDir.resolve(dataStoreFileName).absolutePath }
  }
  single<DeviceIdFetcher> {
    AndroidDeviceIdFetcher(get<DeviceIdDataStore>())
  }
}
