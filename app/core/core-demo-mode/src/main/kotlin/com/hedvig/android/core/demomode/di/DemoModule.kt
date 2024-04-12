package com.hedvig.android.core.demomode.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.demomode.DataStoreDemoManager
import com.hedvig.android.core.demomode.DemoAuthEventListener
import com.hedvig.android.core.demomode.DemoManager
import org.koin.dsl.bind
import org.koin.dsl.module

val demoModule = module {
  single<DemoManager> {
    DataStoreDemoManager(get<DataStore<Preferences>>())
  }
  single<DemoAuthEventListener> {
    DemoAuthEventListener(get<DemoManager>())
  } bind AuthEventListener::class
}
