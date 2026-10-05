package com.hedvig.android.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * The platform's per-app locale. On API 33+ it is shared with the system per-app language settings. Readable and
 * writable without an Activity.
 */
interface AppLocaleStore {
  fun storedLocaleTags(): List<String>

  fun phoneLocaleTags(): List<String>

  /**
   * What to [store] for the app to follow the phone's languages.
   */
  fun localeTagsToFollowPhone(): List<String>

  /**
   * Below API 33 this goes through AppCompat, whose setApplicationLocales should be called after Activity.onCreate,
   * so store only while an Activity exists, as the in-app pickers and the migration do.
   */
  fun store(localeTags: List<String>)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class AndroidAppLocaleStore(
  private val context: Context,
) : AppLocaleStore {
  /**
   * Below API 33 the value lives in AppCompat's own storage, which only this app writes, so it is read from disk once
   * and kept here. Volatile since the first read happens on whichever thread creates the language service.
   */
  @Volatile
  private var storedLocaleTagsBelowApi33: List<String>? = null

  override fun storedLocaleTags(): List<String> {
    if (Build.VERSION.SDK_INT >= 33) {
      return LocaleManagerCompat.getApplicationLocales(context).toLanguageTagList()
    }
    return storedLocaleTagsBelowApi33
      ?: LocaleManagerCompat.getApplicationLocales(context).toLanguageTagList().also { storedLocaleTagsBelowApi33 = it }
  }

  override fun phoneLocaleTags(): List<String> {
    return LocaleManagerCompat.getSystemLocales(context).toLanguageTagList()
  }

  override fun localeTagsToFollowPhone(): List<String> {
    if (Build.VERSION.SDK_INT >= 33) return emptyList()
    // Once AppCompat's value is set in a running process, an empty one makes it apply Android's own pick from the
    // phone's languages, the locale the application's resources resolve to.
    val androidPickedLocaleTag = context.resources.configuration.locales[0].toLanguageTag()
    return localeTagsToFollowPhone(phoneLocaleTags(), androidPickedLocaleTag)
  }

  override fun store(localeTags: List<String>) {
    val languageTags = localeTags.joinToString(separator = ",")
    if (Build.VERSION.SDK_INT >= 33) {
      context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(languageTags)
    } else {
      storedLocaleTagsBelowApi33 = localeTags
      AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTags))
    }
  }
}

private fun LocaleListCompat.toLanguageTagList(): List<String> {
  return (0 until size()).mapNotNull { index -> get(index)?.toLanguageTag() }
}
