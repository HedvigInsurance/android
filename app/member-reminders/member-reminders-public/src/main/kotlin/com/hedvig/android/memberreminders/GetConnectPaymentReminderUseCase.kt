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
import com.hedvig.android.logger.logcat
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, ShowConnectPaymentReminder>
}

internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, ShowConnectPaymentReminder> {
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
          featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER)
        },
      ) { payinStatus, isPayinFeatureEnabled ->
        ensure(isPayinFeatureEnabled) {
          ConnectPaymentReminderError.FeatureFlagNotEnabled
        }
        ensure(payinStatus.currentMember.paymentInformation.status == MemberPaymentConnectionStatus.NEEDS_SETUP) {
          ConnectPaymentReminderError.AlreadySetup
        }
        ShowConnectPaymentReminder
      }
    }.onLeft {
      logcat { "GetConnectPaymentReminderUseCase failed with error:$it" }
    }
  }
}

sealed interface ConnectPaymentReminderError {
  data object FeatureFlagNotEnabled : ConnectPaymentReminderError
  data object AlreadySetup : ConnectPaymentReminderError
  data class NetworkError(val errorMessage: ErrorMessage) : ConnectPaymentReminderError, ErrorMessage by errorMessage
}

object ShowConnectPaymentReminder
