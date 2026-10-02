package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.SetupSwishPayinMutation
import octopus.type.PaymentMethodSetupStatus

internal interface SetupSwishPayinUseCase {
  suspend fun invoke(): Either<ErrorMessage, SetupSwishResponse>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SetupSwishPayinUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : SetupSwishPayinUseCase {
  override suspend fun invoke(): Either<ErrorMessage, SetupSwishResponse> = either {
    val result = apolloClient
      .mutation(SetupSwishPayinMutation())
      .safeExecute()
      .mapLeft { error ->
        logcat(LogPriority.ERROR, error) { "SetupSwishPayinMutation error: $error" }
        ErrorMessage()
      }.bind()
    val output = result.paymentMethodSetupSwishPayin
    when (output.status) {
      PaymentMethodSetupStatus.ACTIVE -> {
        logcat {
          "SetupSwishPayinMutation ACTIVE, url present: ${output.url != null}"
        }
        networkCacheManager.clearCache()
        SetupSwishResponse.Success(output.url, output.orderId)
      }

      PaymentMethodSetupStatus.PENDING -> {
        logcat {
          "SetupSwishPayinMutation PENDING, url present: ${output.url != null}"
        }
        networkCacheManager.clearCache()
        SetupSwishResponse.Pending(output.url, output.orderId)
      }

      PaymentMethodSetupStatus.FAILED, PaymentMethodSetupStatus.UNKNOWN__ -> {
        logcat(LogPriority.WARN) {
          "SetupSwishPayinMutation FAILED: ${output.error?.message}"
        }
        val userMessage = output.error?.message
        SetupSwishResponse.Failure(ErrorMessage(userMessage))
      }
    }
  }
}

internal sealed interface SetupSwishResponse {
  data class Failure(val error: ErrorMessage) : SetupSwishResponse

  data class Success(val url: String?, val orderId: String?) : SetupSwishResponse

  data class Pending(val url: String?, val orderId: String?) : SetupSwishResponse
}

/**
 * A setup the member still has to approve in the Swish app: [successUrl] hands them over to it and
 * [orderId] is what the approval is polled with. Null until the backend returns both, which it does
 * for every setup that actually needs approving.
 */
internal data class SwishSetupOrder(val successUrl: String, val orderId: String)

internal val SetupSwishResponse.order: SwishSetupOrder?
  get() = when (this) {
    is SetupSwishResponse.Success -> swishSetupOrder(url, orderId)
    is SetupSwishResponse.Pending -> swishSetupOrder(url, orderId)
    is SetupSwishResponse.Failure -> null
  }

private fun swishSetupOrder(url: String?, orderId: String?): SwishSetupOrder? {
  if (url == null || orderId == null) return null
  return SwishSetupOrder(url, orderId)
}
