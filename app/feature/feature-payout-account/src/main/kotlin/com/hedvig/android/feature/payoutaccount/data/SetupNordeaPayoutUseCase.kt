package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.SetupNordeaPayoutMutation
import octopus.type.PaymentMethodSetupStatus

internal interface SetupNordeaPayoutUseCase {
  suspend fun invoke(clearingNumber: String, accountNumber: String): Either<ErrorMessage, Unit>
}

internal class SetupNordeaPayoutUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : SetupNordeaPayoutUseCase {
  override suspend fun invoke(clearingNumber: String, accountNumber: String): Either<ErrorMessage, Unit> = either {
    val result = apolloClient
      .mutation(SetupNordeaPayoutMutation(clearingNumber = clearingNumber, accountNumber = accountNumber))
      .safeExecute(::ErrorMessage)
      .bind()

    val output = result.paymentMethodSetupNordeaPayout
    when (output.status) {
      PaymentMethodSetupStatus.FAILED -> {
        raise(ErrorMessage(output.error?.message ?: "Failed to set up payout method"))
      }
      else -> {
        networkCacheManager.clearCache()
      }
    }
  }
}
