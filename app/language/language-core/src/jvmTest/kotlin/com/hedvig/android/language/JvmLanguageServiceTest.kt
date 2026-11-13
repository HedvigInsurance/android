package com.hedvig.android.language

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import java.io.File
import java.util.Locale
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.Rule

class JvmLanguageServiceTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val defaultLocaleBeforeTest = Locale.getDefault()
  private val dataDirectory = createTempDirectory("language-test").toFile()
  private val openStores = mutableListOf<Job>()

  @AfterTest
  fun tearDown() = runBlocking {
    openStores.forEach { it.cancelAndJoin() }
    Locale.setDefault(defaultLocaleBeforeTest)
  }

  @Test
  fun `a picked language survives a new service over the same directory`() = runBlocking {
    JvmLanguageService(openStore(), listOf("en-US")).setLanguage(Language.SV_SE)
    closeStores()

    val restarted = JvmLanguageService(openStore(), listOf("en-US"))

    assertEquals(Language.SV_SE, restarted.language.value)
    assertEquals("sv-SE", Locale.getDefault().toLanguageTag())
  }

  @Test
  fun `with nothing picked the operating system's language decides and nothing is stored`() = runBlocking {
    val store = openStore()

    assertEquals(Language.SV_SE, JvmLanguageService(store, listOf("sv-FI")).language.value)
    assertEquals(Language.EN_SE, JvmLanguageService(store, listOf("en-US")).language.value)
    assertEquals(Language.EN_SE, JvmLanguageService(store, listOf("de-DE")).language.value)
    assertEquals("de-DE", Locale.getDefault().toLanguageTag())
    assertTrue(store.data.first().asMap().isEmpty())
  }

  @Test
  fun `picking the operating system's language clears the pick so the app follows the system again`() = runBlocking {
    val service = JvmLanguageService(openStore(), listOf("en-US"))
    service.setLanguage(Language.SV_SE)
    service.setLanguage(Language.EN_SE)
    assertEquals(Language.EN_SE, service.language.value)
    closeStores()

    val restartedOnSwedishSystem = JvmLanguageService(openStore(), listOf("sv-SE"))

    assertEquals(Language.SV_SE, restartedOnSwedishSystem.language.value)
  }

  @Test
  fun `an unsupported stored language is cleared and the service resolves as with nothing stored`() = runBlocking {
    val store = openStore()
    store.edit { it[stringPreferencesKey(STORED_LANGUAGE_KEY)] = "nb-NO" }

    val service = JvmLanguageService(store, listOf("sv-SE"))

    assertTrue(store.data.first().asMap().isEmpty())
    assertEquals(JvmLanguageService(openEmptyStore(), listOf("sv-SE")).language.value, service.language.value)
    assertEquals("sv-SE", Locale.getDefault().toLanguageTag())
  }

  @Test
  fun `a stored language equal to the operating system's is kept, so it survives a later system change`() =
    runBlocking {
      openStore().edit { it[stringPreferencesKey(STORED_LANGUAGE_KEY)] = "sv-SE" }
      closeStores()

      JvmLanguageService(openStore(), listOf("sv-SE"))
      closeStores()
      val store = openStore()
      val restartedOnEnglishSystem = JvmLanguageService(store, listOf("en-US"))

      assertEquals("sv-SE", store.data.first()[stringPreferencesKey(STORED_LANGUAGE_KEY)])
      assertEquals(Language.SV_SE, restartedOnEnglishSystem.language.value)
    }

  @Test
  fun `a supported stored language different from the operating system's is kept`() = runBlocking {
    val store = openStore()
    store.edit { it[stringPreferencesKey(STORED_LANGUAGE_KEY)] = "sv-SE" }

    val service = JvmLanguageService(store, listOf("en-US"))

    assertEquals("sv-SE", store.data.first()[stringPreferencesKey(STORED_LANGUAGE_KEY)])
    assertEquals(Language.SV_SE, service.language.value)
  }

  private fun openEmptyStore(): DataStore<Preferences> {
    val job = Job()
    openStores += job
    return PreferenceDataStoreFactory.createWithPath(scope = CoroutineScope(Dispatchers.IO + job)) {
      File(createTempDirectory("language-test-empty").toFile(), "test.preferences_pb").absolutePath.toPath()
    }
  }

  private fun openStore(): DataStore<Preferences> {
    val job = Job()
    openStores += job
    return PreferenceDataStoreFactory.createWithPath(scope = CoroutineScope(Dispatchers.IO + job)) {
      File(dataDirectory, "datastore/test.preferences_pb").absolutePath.toPath()
    }
  }

  private suspend fun closeStores() {
    openStores.forEach { it.cancelAndJoin() }
    openStores.clear()
  }
}

private const val STORED_LANGUAGE_KEY = "com.hedvig.android.language.APP_LANGUAGE"
