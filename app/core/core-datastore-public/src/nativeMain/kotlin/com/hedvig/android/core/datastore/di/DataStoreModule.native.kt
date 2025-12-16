package com.hedvig.android.core.datastore.di

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual val platformModule: Module = module {
  // todo ios check path creation here
  val documentDirectory: NSURL? = NSFileManager
    .defaultManager
    .URLForDirectory(
      directory = NSDocumentDirectory,
      inDomain = NSUserDomainMask,
      appropriateForURL = null,
      create = false,
      error = null,
    )
  createDataStore { requireNotNull(documentDirectory).path + "/$dataStoreFileName" }
}
