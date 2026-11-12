package com.hedvig.android.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.JvmDeviceIdFetcher
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

@DependencyGraph(AppScope::class)
internal interface DataStoreTestGraph {
  val dataStore: DataStore<Preferences>
  val deviceIdFetcher: DeviceIdFetcher

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
    ): DataStoreTestGraph
  }
}

class JvmDataStoreBindingsTest {
  private val dataDirectory = createTempDirectory("datastore-test").toFile()
  private val graph = createGraphFactory<DataStoreTestGraph.Factory>().create(dataDirectory)

  @Test
  fun `the data store keeps its file under the data directory`() = runTest {
    graph.dataStore.edit { it[stringPreferencesKey("key")] = "value" }

    assertTrue(File(dataDirectory, "datastore/hedvig_data_store_preferences.preferences_pb").isFile)
  }

  @Test
  fun `the device id fetcher resolves to the JVM implementation`() {
    assertIs<JvmDeviceIdFetcher>(graph.deviceIdFetcher)
  }
}
