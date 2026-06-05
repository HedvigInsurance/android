package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@ContributesTo(AppScope::class)
interface NativeDataStoreMetroProviders {
  @OptIn(ExperimentalForeignApi::class)
  @Provides
  @SingleIn(AppScope::class)
  fun provideDataStore(): DataStore<Preferences> {
    val documentDirectory: NSURL? = NSFileManager
      .defaultManager
      .URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
      )
    return createDataStore { requireNotNull(documentDirectory).path + "/$dataStoreFileName" }
  }
}
