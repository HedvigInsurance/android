package com.hedvig.android.language

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.MainThread
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class AndroidLanguageService(
  context: Context,
  private val appLocaleStore: AppLocaleStore,
) : LanguageService {
  private val mutableLanguage = MutableStateFlow(currentLanguage())
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  init {
    // A change made in the system per-app language settings, or to the phone's languages, arrives as a configuration
    // change of the whole process.
    context.registerComponentCallbacks(
      object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
          refresh()
        }

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() {}
      },
    )
  }

  @MainThread
  override fun setLanguage(language: Language) {
    val localeTags = storedLocaleTagsForPick(language, appLocaleStore.phoneLocaleTags())
    if (localeTags == appLocaleStore.storedLocaleTags()) return
    logcat { "LanguageService: picked $language, storing [${localeTags.joinToString()}]" }
    appLocaleStore.store(localeTags)
    refresh()
  }

  override fun getLocale(): CommonLocale {
    return Locale.forLanguageTag(language.value.toBcp47Format())
  }

  private fun refresh() {
    mutableLanguage.value = currentLanguage()
  }

  private fun currentLanguage(): Language {
    return resolveLanguage(appLocaleStore.storedLocaleTags(), appLocaleStore.phoneLocaleTags())
  }
}
