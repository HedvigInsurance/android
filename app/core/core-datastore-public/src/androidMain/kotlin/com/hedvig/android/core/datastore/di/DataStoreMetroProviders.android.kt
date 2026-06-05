package com.hedvig.android.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.datastore.AndroidDeviceIdFetcher
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdFetcher
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface AndroidDataStoreMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideDataStore(applicationContext: Context): DataStore<Preferences> = createDataStore {
    applicationContext.applicationContext.filesDir.resolve(dataStoreFileName).absolutePath
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideDeviceIdFetcher(deviceIdDataStore: DeviceIdDataStore): DeviceIdFetcher =
    AndroidDeviceIdFetcher(deviceIdDataStore)
}
