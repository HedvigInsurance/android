package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.parZip
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import giraffe.GetPayinMethodStatusQuery
import giraffe.type.PayinMethodStatus

interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, ShowReminder>
}

internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, ShowReminder> {
    return either {
      parZip(
        {
          apolloClient.query(GetPayinMethodStatusQuery())
            .safeExecute()
            .toEither(::ErrorMessage)
            .mapLeft(ConnectPaymentReminderError::NetworkError)
            .bind()
        },
        {
          featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER) // Consider deleting this flag completely?
        },
      ) { payinStatus, isPayinFeatureEnabled ->
        ensure(isPayinFeatureEnabled) {
          ConnectPaymentReminderError.FeatureFlagNotEnabled
        }
        ensure(payinStatus.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
          ConnectPaymentReminderError.AlreadySetup
        }
        ShowReminder
      }
    }
  }
}

sealed interface ConnectPaymentReminderError {
  data object FeatureFlagNotEnabled : ConnectPaymentReminderError
  data object AlreadySetup : ConnectPaymentReminderError
  data class NetworkError(val errorMessage: ErrorMessage) : ConnectPaymentReminderError, ErrorMessage by errorMessage
}

object ShowReminder
