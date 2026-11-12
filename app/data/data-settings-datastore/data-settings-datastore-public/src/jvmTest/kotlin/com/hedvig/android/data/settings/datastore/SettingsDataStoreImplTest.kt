package com.hedvig.android.data.settings.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isEqualTo
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsDataStoreImplTest {
  @get:Rule
  val temporaryFolder: TemporaryFolder = TemporaryFolder()

  // Note: pass a path that does NOT exist yet; DataStore treats a pre-created empty file as corrupt.
  private fun settingsDataStore(fileName: String): SettingsDataStoreImpl {
    val file = File(temporaryFolder.root, fileName)
    return SettingsDataStoreImpl(
      PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() }),
    )
  }

  @Test
  fun `analytics consent defaults to NOT_DECIDED when nothing is stored`() = runTest {
    val store = settingsDataStore("settings1.preferences_pb")
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.NOT_DECIDED)
  }

  @Test
  fun `analytics consent round-trips GRANTED and DENIED`() = runTest {
    val store = settingsDataStore("settings2.preferences_pb")
    store.setAnalyticsConsent(AnalyticsConsent.GRANTED)
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.GRANTED)
    store.setAnalyticsConsent(AnalyticsConsent.DENIED)
    assertThat(store.observeAnalyticsConsent().first()).isEqualTo(AnalyticsConsent.DENIED)
  }
}
