package com.hedvig.android.language

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.locale.AppFormattingLocale
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Resolves the language with the same rule as Android, [resolveLanguage], over the stored in-app pick and the
 * operating system's language. The pick is kept in the preferences DataStore under the desktop data directory, so it
 * survives a restart; with nothing stored the app follows the operating system.
 *
 * The JVM default locale is the stored pick while there is one and the operating system's locale otherwise, like the
 * process locale on Android.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
internal class JvmLanguageService(
  private val dataStore: DataStore<Preferences>,
  private val systemLocaleTags: List<String>,
) : LanguageService {
  @Inject
  constructor(dataStore: DataStore<Preferences>) : this(dataStore, listOf(operatingSystemLocaleTag()))

  private var storedLocaleTags: List<String> = runBlocking { readStoredLocaleTagsClearingUnsupported() }
  private val mutableLanguage = MutableStateFlow(applyCurrentPick())
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  override fun setLanguage(language: Language) {
    val localeTags = storedLocaleTagsForPick(language, systemLocaleTags)
    if (localeTags != storedLocaleTags) {
      logcat { "LanguageService: picked $language, storing [${localeTags.joinToString()}]" }
      runBlocking {
        dataStore.edit { preferences ->
          val localeTag = localeTags.firstOrNull()
          if (localeTag == null) {
            preferences.remove(storedLanguageKey)
          } else {
            preferences[storedLanguageKey] = localeTag
          }
        }
      }
      storedLocaleTags = localeTags
    }
    mutableLanguage.value = applyCurrentPick()
  }

  override fun getLocale(): CommonLocale {
    return language.value.toLocale()
  }

  /**
   * The stored pick. A stored tag that is not a supported language ([isSupportedLocaleTag]) is removed from the store,
   * so the app follows the operating system. A supported pick is kept at every start, also while it matches the
   * operating system's language, so it still applies after that language changes.
   */
  private suspend fun readStoredLocaleTagsClearingUnsupported(): List<String> {
    val storedTag = dataStore.data.first()[storedLanguageKey] ?: return emptyList()
    if (isSupportedLocaleTag(storedTag)) return listOf(storedTag)
    logcat { "LanguageService: clearing unsupported stored language [$storedTag]" }
    dataStore.edit { preferences -> preferences.remove(storedLanguageKey) }
    return emptyList()
  }

  /**
   * Updates the JVM default locale and [AppFormattingLocale] for the current pick and returns the language to show.
   */
  private fun applyCurrentPick(): Language {
    val language = resolveLanguage(storedLocaleTags, systemLocaleTags)
    val processLocaleTag = storedLocaleTags.firstOrNull() ?: systemLocaleTags.firstOrNull()
    if (processLocaleTag != null) {
      Locale.setDefault(Locale.forLanguageTag(processLocaleTag))
    }
    AppFormattingLocale.current = language.toLocale()
    return language
  }
}

private val storedLanguageKey = stringPreferencesKey("com.hedvig.android.language.APP_LANGUAGE")

/**
 * The operating system's locale as the JVM read it at startup. Read from the system properties, which
 * [Locale.setDefault] leaves unchanged.
 */
private fun operatingSystemLocaleTag(): String {
  val language = System.getProperty("user.language").orEmpty()
  val country = System.getProperty("user.country").orEmpty()
  return if (country.isEmpty()) language else "$language-$country"
}

private fun Language.toLocale(): Locale = Locale.forLanguageTag(toBcp47Format())
