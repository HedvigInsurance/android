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

  fun store(localeTags: List<String>)
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class AndroidAppLocaleStore(
  private val context: Context,
) : AppLocaleStore {
  /**
   * Below API 33 the value lives in AppCompat's own storage, which only this app writes, so it is read from disk once.
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
