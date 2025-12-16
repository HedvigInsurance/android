package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<DataStore<Preferences>> {
    val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
    createDataStore(producePath = { file.absolutePath })
  }
}
