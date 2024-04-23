package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder>
}

internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder> {
    return either {
      val result = apolloClient.query(GetPayinMethodStatusQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(ConnectPaymentReminderError::NetworkError)
        .bind()
      val missingPaymentsContractTerminationDate = result.currentMember.activeContracts
        .filter { it.terminationDueToMissedPayments }
        .sortedBy { it.terminationDate }
        .firstOrNull()?.terminationDate
      if (missingPaymentsContractTerminationDate != null) {
        PaymentReminder.ShowMissingPaymentsReminder(missingPaymentsContractTerminationDate)
      } else {
        val payStatus = result.currentMember.paymentInformation.status
        ensure(payStatus == MemberPaymentConnectionStatus.NEEDS_SETUP) {
          ConnectPaymentReminderError.AlreadySetup
        }
        PaymentReminder.ShowConnectPaymentReminder
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

sealed interface PaymentReminder {
  data object ShowConnectPaymentReminder : PaymentReminder

  data class ShowMissingPaymentsReminder(val terminationDate: LocalDate) : PaymentReminder
}
