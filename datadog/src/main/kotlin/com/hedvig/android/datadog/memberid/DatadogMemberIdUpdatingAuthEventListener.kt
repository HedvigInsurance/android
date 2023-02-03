package com.hedvig.android.datadog.memberid

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.datadog.android.rum.GlobalRum
import com.hedvig.android.auth.event.AuthEventListener
import slimber.log.v
import java.util.concurrent.TimeUnit

class DatadogMemberIdUpdatingAuthEventListener(
  private val context: Context,
) : AuthEventListener {
  override suspend fun loggedOut() {
    v { "MemberIdTrackingAuthenticationListener cancelling all work with tag: ${DatadogMemberIdTrackingWorker.TAG}" }
    WorkManager.getInstance(context).cancelAllWorkByTag(DatadogMemberIdTrackingWorker.TAG)
    v { "MemberIdTrackingAuthenticationListener removing rum key:$MEMBER_ID_TRACKING_KEY" }
    GlobalRum.removeAttribute(MEMBER_ID_TRACKING_KEY)
  }

  override suspend fun loggedIn() {
    v { "MemberIdTrackingAuthenticationListener triggering DatadogMemberIdTrackingWorker" }
    WorkManager.getInstance(context).enqueue(
      OneTimeWorkRequest.Builder(DatadogMemberIdTrackingWorker::class.java)
        .addTag(DatadogMemberIdTrackingWorker.TAG)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
        .build(),
    )
  }
}
