package com.hedvig.android.notification.badge.data.payment

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.MissedPaymentQuery

interface GetMissedPaymentIdUseCase {
  fun invoke(): Flow<Boolean>
}

internal class GetMissedPaymentIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMissedPaymentIdUseCase {
  override fun invoke(): Flow<Boolean> {
    return apolloClient
      .query(MissedPaymentQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow()
      .map { result ->
        result.fold(
          {
            logcat(operationError = it) {
              "Error when loading missed payment: $it"
            }
            false
          },
          { data ->
            data.currentMember.missedChargeIdToChargeManually != null
          },
        )
      }
  }
}
