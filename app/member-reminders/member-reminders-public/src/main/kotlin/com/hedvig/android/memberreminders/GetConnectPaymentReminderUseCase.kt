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
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetConnectPaymentReminderUseCase {
  suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder>
}

internal class GetConnectPaymentReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getOnlyHasNonPayingContractsUseCaseProvider: Provider<GetOnlyHasNonPayingContractsUseCase>,
  private val marketManager: MarketManager,
) : GetConnectPaymentReminderUseCase {
  override suspend fun invoke(): Either<ConnectPaymentReminderError, PaymentReminder> {
    return either {
      ensure(marketManager.selectedMarket().filterNotNull().first() == Market.SE) {
        ConnectPaymentReminderError.DomainError.NotSwedishMarket
      }
      val onlyHasNonPayingContracts = getOnlyHasNonPayingContractsUseCaseProvider.provide().invoke().getOrNull() == true
      ensure(onlyHasNonPayingContracts == false) {
        ConnectPaymentReminderError.DomainError.NonPayingMember
      }
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
      val payStatus = result.currentMember.paymentInformation.status
      ensure(payStatus == MemberPaymentConnectionStatus.NEEDS_SETUP) {
        ConnectPaymentReminderError.DomainError.AlreadySetup
      }
      PaymentReminder.ShowConnectPaymentReminder
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

    data object NotSwedishMarket : DomainError
  }
}

sealed interface PaymentReminder {
  data object ShowConnectPaymentReminder : PaymentReminder

  data class ShowMissingPaymentsReminder(val terminationDate: LocalDate) : PaymentReminder
}
