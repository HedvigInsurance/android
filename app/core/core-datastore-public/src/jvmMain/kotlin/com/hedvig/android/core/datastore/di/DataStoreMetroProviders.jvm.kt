package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.io.File

@ContributesTo(AppScope::class)
interface JvmDataStoreMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideDataStore(
    @DesktopDataDirectory dataDirectory: File,
  ): DataStore<Preferences> = createDataStore {
    File(dataDirectory, dataStoreFileName).absolutePath
  }
}
