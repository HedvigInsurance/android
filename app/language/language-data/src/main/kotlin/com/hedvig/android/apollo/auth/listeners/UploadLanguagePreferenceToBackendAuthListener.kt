package com.hedvig.android.apollo.auth.listeners

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class UploadLanguagePreferenceToBackendAuthListener(
  private val languageService: LanguageService,
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
  private val uploadedLanguageStore: UploadedLanguageStore,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    val language = languageService.getLanguage()
    uploadLanguagePreferenceToBackendUseCase.invoke(language).onRight {
      uploadedLanguageStore.setLastUploadedLanguage(language)
    }
  }
}
