package com.hedvig.android.datadog.memberid

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.getOrElse
import com.apollographql.apollo3.ApolloClient
import com.datadog.android.rum.GlobalRum
import com.hedvig.android.apollo.graphql.MemberIdQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import slimber.log.d
import slimber.log.i
import slimber.log.v

internal class DatadogMemberIdTrackingWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val apolloClient: ApolloClient by inject()

  override suspend fun doWork(): Result {
    v { "DatadogMemberIdTrackingWorker starting work" }
    val data = apolloClient
      .query(MemberIdQuery())
      .safeExecute()
      .toEither()
      .getOrElse { return Result.retry() }

    val memberId = data.member.id
    if (memberId == null) {
      d { "DatadogMemberIdTrackingWorker queried for memberId but got null member id" }
      return Result.failure()
    }
    i { "DatadogMemberIdTrackingWorker adding to rum member_id :$memberId" }
    GlobalRum.addAttribute(MEMBER_ID_TRACKING_KEY, memberId)
    return Result.success()
  }

  companion object {
    internal const val TAG = "DatadogMemberIdTrackingWorker"
  }
}
