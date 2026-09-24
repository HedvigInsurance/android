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
  fun invoke(): Flow<PaymentsNotificationBadge?>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetPaymentsNotificationBadgeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPaymentsNotificationBadgeUseCase {
  override fun invoke(): Flow<PaymentsNotificationBadge?> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val badge = apolloClient
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
              { data -> data.currentMember.toPaymentsNotificationBadge() },
            )
          }
          .firstOrNull()

        emit(badge)

        // A missed payment is re-checked so the dot clears once the member pays it. The pre-charge
        // notice changes about once a day, so one read per session is enough.
        if (badge != PaymentsNotificationBadge.MissedPayment) {
          break
        }

        delay(15.seconds)
      }
    }
  }
}

private fun MissedPaymentQuery.Data.CurrentMember.toPaymentsNotificationBadge(): PaymentsNotificationBadge? {
  return when {
    showPreChargeNotice -> PaymentsNotificationBadge.PreChargeNotice
    missedChargeIdToChargeManually != null -> PaymentsNotificationBadge.MissedPayment
    else -> null
  }
}
