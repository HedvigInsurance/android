package com.hedvig.android.notification.badge.data.payment

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import octopus.MissedPaymentQuery

internal interface GetPaymentsNotificationBadgeUseCase {
  /** Emits null when the payments state could not be loaded. */
  fun invoke(): Flow<PaymentsNotificationBadgeData?>
}

internal data class PaymentsNotificationBadgeData(
  val hasMissedPayment: Boolean,
  /** The backend returns a new id for each upcoming charge or retry occasion, so a seen id never needs a dot again. */
  val chargeNoticeIds: Set<String>,
)

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetPaymentsNotificationBadgeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPaymentsNotificationBadgeUseCase {
  override fun invoke(): Flow<PaymentsNotificationBadgeData?> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val badgeData = apolloClient
          .query(MissedPaymentQuery())
          .fetchPolicy(FetchPolicy.CacheAndNetwork)
          .safeFlow {
            logcat { "GetPaymentsNotificationBadgeUseCaseImpl error: $it" }
            ErrorMessage()
          }
          .map { result ->
            result.fold(
              {
                logcat { "GetPaymentsNotificationBadgeUseCaseImpl: error when loading payments badge: $it" }
                null
              },
              { data -> data.currentMember.toPaymentsNotificationBadgeData() },
            )
          }
          .firstOrNull()

        emit(badgeData)

        // A missed payment is re-checked so the dot clears once the member pays it. The charge
        // notices change about once a day, so one read per session is enough.
        if (badgeData?.hasMissedPayment != true) {
          break
        }

        delay(15.seconds)
      }
    }
  }
}

private fun MissedPaymentQuery.Data.CurrentMember.toPaymentsNotificationBadgeData(): PaymentsNotificationBadgeData {
  return PaymentsNotificationBadgeData(
    hasMissedPayment = missedChargeIdToChargeManually != null,
    chargeNoticeIds = setOfNotNull(showPreChargeNotice, showRetryChargeNotice),
  )
}
