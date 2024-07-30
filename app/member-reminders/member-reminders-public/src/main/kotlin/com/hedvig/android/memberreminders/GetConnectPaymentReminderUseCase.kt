package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder>
}

internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getOnlyHasNonPayingContractsUseCaseProvider: Provider<GetOnlyHasNonPayingContractsUseCase>,
) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder> {
    return either {
      val onlyHasNonPayingContracts = getOnlyHasNonPayingContractsUseCaseProvider.provide().invoke().getOrNull() == true
      ensure(onlyHasNonPayingContracts == false) {
        ConnectPaymentReminderError.NonPayingMember
      }
      val result = apolloClient.query(GetPayinMethodStatusQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(ConnectPaymentReminderError::NetworkError)
        .bind()
      val missingPaymentsContractTerminationDate = result.currentMember.activeContracts
        .filter { it.terminationDueToMissedPayments }
        .sortedBy { it.terminationDate }
        .firstOrNull()
        ?.terminationDate
      if (missingPaymentsContractTerminationDate != null) {
        return@either PaymentReminder.ShowMissingPaymentsReminder(missingPaymentsContractTerminationDate)
      }
      val payStatus = result.currentMember.paymentInformation.status
      ensure(payStatus == MemberPaymentConnectionStatus.NEEDS_SETUP) {
        ConnectPaymentReminderError.AlreadySetup
      }
      PaymentReminder.ShowConnectPaymentReminder
    }.onLeft {
      logcat { "GetConnectPaymentReminderUseCase failed with error:$it" }
    }
  }
}

sealed interface ConnectPaymentReminderError {
  data object AlreadySetup : ConnectPaymentReminderError

  data object NonPayingMember : ConnectPaymentReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : ConnectPaymentReminderError, ErrorMessage by errorMessage
}

sealed interface PaymentReminder {
  data object ShowConnectPaymentReminder : PaymentReminder

  data class ShowMissingPaymentsReminder(val terminationDate: LocalDate) : PaymentReminder
}
