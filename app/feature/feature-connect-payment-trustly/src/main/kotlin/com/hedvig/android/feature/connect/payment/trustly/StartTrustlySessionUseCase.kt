package com.hedvig.android.feature.connect.payment.trustly

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.connect.payment.trustly.data.TrustlyCallback
import com.hedvig.android.logger.logcat
import octopus.InitiateTrustlyConnectPaymentSessionMutation

internal class StartTrustlySessionUseCase(
  private val apolloClient: ApolloClient,
  private val trustlyCallback: TrustlyCallback,
) {
  suspend fun invoke(): Either<ErrorMessage, TrustlyInitiateProcessUrl> {
    return either {
      val data = apolloClient
        .mutation(
          InitiateTrustlyConnectPaymentSessionMutation(
            successUrl = trustlyCallback.successUrl,
            failureUrl = trustlyCallback.failureUrl,
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
      logcat { "StartTrustlySessionUseCase received: ${data.registerDirectDebit2}" }
      TrustlyInitiateProcessUrl(data.registerDirectDebit2.url)
    }
  }
}

@JvmInline
internal value class TrustlyInitiateProcessUrl(val url: String)
