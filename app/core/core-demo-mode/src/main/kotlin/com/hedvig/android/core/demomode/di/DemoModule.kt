package com.hedvig.android.core.demomode.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.demomode.DataStoreDemoManager
import com.hedvig.android.core.demomode.DemoManager
import org.koin.dsl.module

val demoModule = module {
  single<DemoManager> {
    DataStoreDemoManager(get<DataStore<Preferences>>())
  }
}
