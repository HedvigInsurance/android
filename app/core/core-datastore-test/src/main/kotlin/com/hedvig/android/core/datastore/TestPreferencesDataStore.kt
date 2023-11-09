package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus

private const val testPreferencesDatastoreFileName = "hedvig_test_data_store_preferences"

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore-preferences/src/main/java/androidx/datastore/preferences/PreferenceDataStoreFile.kt;l=37-38
private const val preferencesDatastoreFileExtension = ".preferences_pb"

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
private const val preferencesDataStoreFileDirectory = "datastore"

/**
 * @param datastoreTestFileDirectory you probably want to use [org.junit.rules.TemporaryFolder] to generate this file.
 * Sample:
 * ```
 * @get:Rule
 * val testFolder = TemporaryFolder()
 *
 * // Later in the test setup
 * TestPreferencesDataStore(
 *   datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
 *   coroutineScope = coroutineScope,
 * )
 * ```
 */
class TestPreferencesDataStore(
  datastoreTestFileDirectory: File,
  coroutineScope: CoroutineScope,
) : DataStore<Preferences> by PreferenceDataStoreFactory.create(
    // + Dispatchers.Unconfined is important so that the DataStore coroutines execute eagerly. This is important because
    // when we test this with a runBlocking in play, we don't have control over when to do `runCurrent()`.
    scope = coroutineScope + Dispatchers.Unconfined,
    produceFile = {
      val fileNameWithDatastoreExtension = "$testPreferencesDatastoreFileName$preferencesDatastoreFileExtension"
      File(datastoreTestFileDirectory, "$preferencesDataStoreFileDirectory/$fileNameWithDatastoreExtension")
    },
  )
