package com.hedvig.android.apollo.auth.listeners

import com.hedvig.android.auth.event.AuthEventListener

internal class UploadLanguagePreferenceToBackendAuthListener(
  private val uploadLanguagePreferenceToBackendUseCase: UploadLanguagePreferenceToBackendUseCase,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    uploadLanguagePreferenceToBackendUseCase.invoke()
  }
}
