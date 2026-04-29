package com.hedvig.android.notification.badge.data.payment

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import octopus.MissedPaymentQuery

interface GetIfMissedPaymentUseCase {
  fun invoke(): Flow<Boolean>
}

internal class GetIfMissedPaymentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetIfMissedPaymentUseCase {
  override fun invoke(): Flow<Boolean> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient
            .query(MissedPaymentQuery())
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .safeFlow {
              logcat { "GetIfMissedPaymentUseCaseImpl error: $it" }
              ErrorMessage()
            }
            .map { result ->
              result.fold(
                {
                  logcat { "GetIfMissedPaymentUseCaseImpl: error when loading missed payment: $it" }
                  false
                },
                { data ->
                  data.currentMember.missedChargeIdToChargeManually != null
                },
              )
            },
        )
        delay(5.seconds)
      }
    }
  }
}
