package com.hedvig.android.notification.firebase

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A central place to handle firebase push tokens. Currently called both from the login auth event, and when a new
 * token is received by our [com.hedvig.android.notification.firebase.PushNotificationService]
 */
internal class FCMTokenManager(
  private val applicationContext: Context,
  private val fcmTokenStorage: FCMTokenStorage,
) {
  suspend fun saveLocallyAndUploadTokenToBackend(token: String) {
    fcmTokenStorage.saveToken(token)
    WorkManager
      .getInstance(applicationContext)
      .cancelAllWorkByTag(FIREBASE_PUSH_TOKEN_MUTATION_WORKER_TAG)
      .await()
    WorkManager.getInstance(applicationContext).enqueue(
      OneTimeWorkRequestBuilder<FCMTokenUploadWorker>()
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30.seconds.toJavaDuration())
        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .addTag(FIREBASE_PUSH_TOKEN_MUTATION_WORKER_TAG)
        .build(),
    )
  }

  suspend fun deleteTokenLocallyAndFromFirebaseMessaging() {
    fcmTokenStorage.clearToken()
    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(FIREBASE_PUSH_TOKEN_MUTATION_WORKER_TAG)
    FirebaseMessaging.getInstance().deleteToken().await()
  }

  companion object {
    private const val FIREBASE_PUSH_TOKEN_MUTATION_WORKER_TAG = "FIREBASE_PUSH_TOKEN_MUTATION_WORKER_TAG"
  }
}
