package com.hedvig.android.notification.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.android.auth.event.AuthEventListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import slimber.log.d
import slimber.log.e

internal class FCMTokenAuthEventListener(
  private val fcmTokenManager: FCMTokenManager,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    d { "Logged in, fetching a new FCM token" }
    try {
      val freshToken = FirebaseMessaging.getInstance().token.await()
      d { "Logged in, so storing a new push token:$freshToken" }
      fcmTokenManager.saveLocallyAndUploadTokenToBackend(freshToken)
    } catch (throwable: Throwable) {
      if (throwable is CancellationException) {
        throw throwable
      }
      e(throwable) { "Failed to fetch new token after logging in" }
    }
  }

  override suspend fun loggedOut() {
    d { "Logged out, so clearing the existing push token" }
    fcmTokenManager.deleteTokenLocallyAndFromFirebaseMessaging()
  }
}
