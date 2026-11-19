package com.hedvig.android.apollo.auth.listeners

import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps the network cache and the member's language on the backend in line with the app language, whichever way it
 * changed: an in-app picker, the system per-app language settings, or the phone's languages.
 */
@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class LanguageSync(
  private val languageService: LanguageService,
  private val networkCacheManager: NetworkCacheManager,
  private val authTokenService: AuthTokenService,
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
  private val uploadedLanguageStore: UploadedLanguageStore,
  private val applicationScope: ApplicationScope,
) : Initializable {
  override fun initialize() {
    applicationScope.launch {
      var previousLanguage: Language? = null
      languageService.language.collect { language ->
        if (previousLanguage != null && previousLanguage != language) {
          logcat { "LanguageSync: app language changed from $previousLanguage to $language" }
          networkCacheManager.clearCache()
        }
        previousLanguage = language
        uploadIfLoggedInAndChanged(language)
      }
    }
  }

  private suspend fun uploadIfLoggedInAndChanged(language: Language) {
    val authStatus = authTokenService.authStatus.filterNotNull().first()
    if (authStatus !is AuthStatus.LoggedIn) return
    if (uploadedLanguageStore.lastUploadedLanguage() == language) return
    uploadLanguagePreferenceToBackendUseCase.invoke(language).onRight {
      uploadedLanguageStore.setLastUploadedLanguage(language)
    }
  }
}
