package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.SetupSwishPayoutMutation
import octopus.type.PaymentMethodSetupStatus

internal class SetupSwishPayoutUseCase(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) {
  suspend fun invoke(phoneNumber: String): Either<ErrorMessage, Unit> = either {
    val result = apolloClient
      .mutation(SetupSwishPayoutMutation(phoneNumber = phoneNumber))
      .safeExecute(::ErrorMessage)
      .bind()

    val output = result.paymentMethodSetupSwishPayout
    when (output.status) {
      PaymentMethodSetupStatus.FAILED -> {
        raise(ErrorMessage(output.error?.message))
      }

      else -> {
        networkCacheManager.clearCache()
      }
    }
  }
}
