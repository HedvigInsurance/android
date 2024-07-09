package com.hedvig.android.notification.firebase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.MemberDeviceRegisterMutation

/**
 * When we get a new firebase token, we need to make sure that we do not fail to upload it to the backend. If we are in
 * a situation with bad internet or the network request fails for whatever reason, we need to retry until it works.
 * Important to also make sure to cancel this job when we've logged out again, so that we do not keep trying to upload
 * the token after we've already logged out and that token is no longer relevant.
 */
internal class FCMTokenUploadWorker(
  context: Context,
  params: WorkerParameters,
  private val apolloClient: ApolloClient,
  private val fcmTokenStorage: FCMTokenStorage,
  private val authTokenService: AuthTokenService,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val storedToken = fcmTokenStorage.getToken().first()
    if (storedToken == null) {
      logcat { "stored token was null, no longer need to report something to backend" }
      return Result.success()
    }
    val authStatus = authTokenService.authStatus.value
    if (authStatus !is AuthStatus.LoggedIn) {
      logcat { "We're not logged in, we shouldn't upload a token to the backend" }
      return Result.success()
    }
    return apolloClient
      .mutation(MemberDeviceRegisterMutation(storedToken))
      .safeExecute()
      .toEither()
      .fold(
        ifLeft = {
          logcat { "NotificationRegisterDeviceMutation failed with token:$storedToken. Will retry later. Error:$it" }
          Result.retry()
        },
        ifRight = {
          logcat { "NotificationRegisterDeviceMutation success with token:$storedToken" }
          Result.success()
        },
      )
  }
}
