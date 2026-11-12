package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.PaymentMethodSetupStatusQuery
import octopus.type.PaymentMethodSetupStatus

/** Where a setup order the member is approving in the Swish app has got to. */
internal sealed interface SwishPayinSetupStatus {
  data object Active : SwishPayinSetupStatus

  data object Pending : SwishPayinSetupStatus

  data class Failed(val message: String?) : SwishPayinSetupStatus
}

internal interface GetSwishPayinSetupStatusUseCase {
  suspend fun invoke(orderId: String): Either<ErrorMessage, SwishPayinSetupStatus>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetSwishPayinSetupStatusUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : GetSwishPayinSetupStatusUseCase {
  override suspend fun invoke(orderId: String): Either<ErrorMessage, SwishPayinSetupStatus> = either {
    val output = apolloClient
      .query(PaymentMethodSetupStatusQuery(orderId))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()
      .paymentMethodSetupStatus

    when (output?.status) {
      PaymentMethodSetupStatus.ACTIVE -> {
        // The new method is absent from every cached payment query until the next fetch.
        networkCacheManager.clearCache()
        SwishPayinSetupStatus.Active
      }

      PaymentMethodSetupStatus.FAILED -> {
        SwishPayinSetupStatus.Failed(output.error?.message)
      }

      // A null output is an order the backend does not know about yet, which is indistinguishable
      // from one still being processed, so both keep the caller waiting rather than failing it.
      PaymentMethodSetupStatus.PENDING,
      PaymentMethodSetupStatus.UNKNOWN__,
      null,
      -> {
        SwishPayinSetupStatus.Pending
      }
    }
  }
}
