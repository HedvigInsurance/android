package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.LocalDate
import octopus.GetPayinMethodStatusQuery
import octopus.type.MissingPaymentConnection

internal interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder> {
    return either {
      val result = apolloClient.query(GetPayinMethodStatusQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute(::ErrorMessage)
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

      val missingConnection = result.currentMember.paymentMethods.missingConnection
      when (missingConnection) {
        MissingPaymentConnection.PAYIN -> return@either PaymentReminder.ShowConnectPaymentReminder
        MissingPaymentConnection.PAYOUT -> return@either PaymentReminder.ShowConnectPayoutReminder
        MissingPaymentConnection.UNKNOWN__ , null -> raise(ConnectPaymentReminderError.DomainError.AlreadySetup)
      }

    }.onLeft {
      if (it !is ConnectPaymentReminderError.DomainError) {
        logcat { "GetConnectPaymentReminderUseCase failed with error:$it" }
      }
    }
  }
}

sealed interface ConnectPaymentReminderError {
  data class NetworkError(val errorMessage: ErrorMessage) : ConnectPaymentReminderError, ErrorMessage by errorMessage

  sealed interface DomainError : ConnectPaymentReminderError {
    data object AlreadySetup : DomainError

    data object NonPayingMember : DomainError
  }
}

sealed interface PaymentReminder {
  data object ShowConnectPaymentReminder : PaymentReminder

  data object ShowConnectPayoutReminder : PaymentReminder

  data class ShowMissingPaymentsReminder(val terminationDate: LocalDate) : PaymentReminder
}
