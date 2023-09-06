package com.hedvig.android.notification.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

internal class FCMTokenAuthEventListener(
  private val fcmTokenManager: FCMTokenManager,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    logcat { "Logged in, fetching a new FCM token" }
    try {
      val freshToken = FirebaseMessaging.getInstance().token.await()
      logcat { "Logged in, so storing a new push token:$freshToken" }
      fcmTokenManager.saveLocallyAndUploadTokenToBackend(freshToken)
    } catch (throwable: Throwable) {
      if (throwable is CancellationException) {
        throw throwable
      }
      logcat(LogPriority.ERROR, throwable) { "Failed to fetch new token after logging in" }
    }
  }

  override suspend fun loggedOut() {
    logcat { "Logged out, so clearing the existing push token" }
    fcmTokenManager.deleteTokenLocallyAndFromFirebaseMessaging()
  }
}
