package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.JvmDeviceIdFetcher
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataStoreModule: Module = module {
  single<DataStore<Preferences>> {
    val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
    createDataStore(producePath = { file.absolutePath })
  }
  single<DeviceIdFetcher> {
    JvmDeviceIdFetcher(get<DeviceIdDataStore>())
  }
}
