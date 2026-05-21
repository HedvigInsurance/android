package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.SetupSwishPayinMutation
import octopus.type.PaymentMethodSetupStatus
import octopus.type.PaymentMethodSetupSwishInput

internal interface SetupSwishPayinUseCase {
  suspend fun invoke(phoneNumber: String): Either<ErrorMessage, SetupSwishResponse>
}

internal class SetupSwishPayinUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : SetupSwishPayinUseCase {
  override suspend fun invoke(phoneNumber: String): Either<ErrorMessage, SetupSwishResponse> = either {
    val result = apolloClient
      .mutation(SetupSwishPayinMutation(PaymentMethodSetupSwishInput(phoneNumber)))
      .safeExecute {
        logcat {
          "Mariia: SetupSwishPayinMutation error: $it"
        }
        raise(ErrorMessage())
      }
      .bind()

    val output = result.paymentMethodSetupSwishPayin
    when (output.status) {
      PaymentMethodSetupStatus.ACTIVE -> {
        logcat {
          "Mariia: SetupSwishPayinMutation ACTIVE url: $output.url"
        }
        networkCacheManager.clearCache()
        SetupSwishResponse.Success(output.url)
      }

      PaymentMethodSetupStatus.PENDING -> {
        logcat {
          "Mariia: SetupSwishPayinMutation PENDING url: $output.url"
        }
        networkCacheManager.clearCache()
        SetupSwishResponse.Pending(output.url)
      }

      PaymentMethodSetupStatus.FAILED, PaymentMethodSetupStatus.UNKNOWN__ -> {
        logcat {
          "SetupSwishPayinMutation failed with: output.error?.message"
        }
        val userMessage = output.error?.message
        SetupSwishResponse.Failure(ErrorMessage(userMessage))
      }
    }
  }
}

internal sealed interface SetupSwishResponse {
  data class Failure(val error: ErrorMessage) : SetupSwishResponse

  data class Success(val url: String?) : SetupSwishResponse

  data class Pending(val url: String?) : SetupSwishResponse
}
