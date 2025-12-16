package com.hedvig.android.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  // https://developer.android.com/kotlin/multiplatform/datastore#creating-datastore
  single<DataStore<Preferences>> {
    val context = get<Context>()
    createDataStore { context.applicationContext.filesDir.resolve(dataStoreFileName).absolutePath }
  }
}
