package com.hedvig.android.feature.home.home.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DismissedShopSessionsStorageTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `nothing is dismissed to begin with`() = runTest {
    val storage = DismissedShopSessionsStorageImpl(testDataStore())

    assertThat(storage.observeDismissedSessionIds().first()).isEmpty()
  }

  @Test
  fun `a dismissed session id is observable`() = runTest {
    val storage = DismissedShopSessionsStorageImpl(testDataStore())

    storage.dismiss("session-1")

    assertThat(storage.observeDismissedSessionIds().first()).containsExactlyInAnyOrder("session-1")
  }

  @Test
  fun `dismissing a second session keeps the first one`() = runTest {
    val storage = DismissedShopSessionsStorageImpl(testDataStore())

    storage.dismiss("session-1")
    storage.dismiss("session-2")

    assertThat(storage.observeDismissedSessionIds().first())
      .containsExactlyInAnyOrder("session-1", "session-2")
  }

  @Test
  fun `dismissals are kept in the data store rather than in memory`() = runTest {
    val dataStore = testDataStore()
    DismissedShopSessionsStorageImpl(dataStore).dismiss("session-1")

    val freshStorage = DismissedShopSessionsStorageImpl(dataStore)

    assertThat(freshStorage.observeDismissedSessionIds().first()).containsExactlyInAnyOrder("session-1")
  }

  @Test
  fun `a value of the wrong type under the key reads as nothing dismissed`() = runTest {
    val dataStore = testDataStore()
    // An older build of this feature stored a String under a key of this name.
    dataStore.edit { it[stringPreferencesKey(DismissedShopSessionsStorageImpl.KEY_NAME)] = "not-a-set" }

    val dismissed = DismissedShopSessionsStorageImpl(dataStore).observeDismissedSessionIds().first()

    assertThat(dismissed).isEmpty()
  }

  private fun TestScope.testDataStore() = TestPreferencesDataStore(
    datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
    coroutineScope = backgroundScope,
  )
}
