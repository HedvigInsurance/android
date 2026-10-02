package com.hedvig.android.language

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.MainThread
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
    AppFormattingLocale.current = mutableLanguage.value.toLocale()
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
    if (localeTags == appLocaleStore.storedLocaleTags()) {
      if (language == this.language.value) return
      // The stored value already asks for this language but the process still renders another one, which happens when
      // Android keeps a cleared app locale applied to the running process. Storing the language explicitly first makes
      // Android apply a real change.
      logcat { "LanguageService: picked $language while rendering ${this.language.value}, storing it explicitly first" }
      appLocaleStore.store(listOf(language.toBcp47Format()))
    }
    logcat { "LanguageService: picked $language, storing [${localeTags.joinToString()}]" }
    appLocaleStore.store(localeTags)
    refresh()
  }

  override fun getLocale(): CommonLocale {
    return language.value.toLocale()
  }

  private fun refresh() {
    val language = currentLanguage()
    AppFormattingLocale.current = language.toLocale()
    mutableLanguage.value = language
  }

  private fun currentLanguage(): Language {
    if (Build.VERSION.SDK_INT >= 33) {
      // The first entry of the process locale list is what Compose, and so Compose Multiplatform, renders strings in.
      // It is the app locale when one is applied to the process, otherwise the phone's first language.
      return languageOf(LocaleList.getDefault()[0].toLanguageTag())
    }
    // Below API 33 AppCompat applies the stored locale to Activities only, so the process locale list does not show it.
    return resolveLanguage(appLocaleStore.storedLocaleTags(), appLocaleStore.phoneLocaleTags())
  }
}

private fun Language.toLocale(): Locale = Locale.forLanguageTag(toBcp47Format())
