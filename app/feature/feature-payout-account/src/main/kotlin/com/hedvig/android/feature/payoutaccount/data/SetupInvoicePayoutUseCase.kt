package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.SetupInvoicePayoutMutation
import octopus.type.PaymentMethodInvoiceDelivery
import octopus.type.PaymentMethodSetupStatus

internal class SetupInvoicePayoutUseCase(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) {
  suspend fun invoke(): Either<ErrorMessage, Unit> = either {
    FakePayoutAccountStorage.currentMethod = PayoutAccount.Invoice(
      delivery = PaymentMethodInvoiceDelivery.KIVRA,
      email = null,
      isPending = false,
    )
    return@either
    val result = apolloClient
      .mutation(SetupInvoicePayoutMutation())
      .safeExecute(::ErrorMessage)
      .bind()

    val output = result.paymentMethodSetupInvoicePayin
    when (output.status) {
      PaymentMethodSetupStatus.FAILED -> {
        raise(ErrorMessage(output.error?.message ?: "Failed to set up invoice payout"))
      }

      else -> {
        networkCacheManager.clearCache()
      }
    }
  }
}
