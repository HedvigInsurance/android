package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.core.datastore.DeviceIdDataStoreImpl
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DataStoreMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideDeviceIdDataStore(
    dataStore: DataStore<Preferences>,
    applicationScope: ApplicationScope,
  ): DeviceIdDataStore = DeviceIdDataStoreImpl(dataStore, applicationScope)
}
