package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.MemberPaymentDetailsQuery
import octopus.fragment.MemberPaymentMethodFragment
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodBankAccountDetailsDetails
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodInvoiceDetailsDetails
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodSwishDetailsDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

internal interface GetMemberPaymentsDetailsUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberPaymentsDetails>
}

internal class GetMemberPaymentsDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberPaymentsDetailsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberPaymentsDetails> {
    return either {
      val result = apolloClient.query(MemberPaymentDetailsQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute(::ErrorMessage)
        .onLeft {
          logcat(LogPriority.WARN) { "GetMemberPaymentsDetailsUseCase returned error: $it" }
        }
        .bind()
      val paymentMethods = result.currentMember.paymentMethods
      val payinMethod = paymentMethods.defaultPayinMethod
        ?: paymentMethods.payinMethods.find { it.isDefault && it.status == MemberPaymentMethodStatus.ACTIVE }
      if (payinMethod == null) {
        logcat(LogPriority.WARN) { "GetMemberPaymentsDetailsUseCase no active default payin method" }
        raise(ErrorMessage())
      }
      val paymentMethod = payinMethod.provider.toPaymentMethod()
      if (paymentMethod == null) {
        logcat(LogPriority.WARN) { "GetMemberPaymentsDetailsUseCase unknown provider: ${payinMethod.provider}" }
        raise(ErrorMessage())
      }
      MemberPaymentsDetails(
        paymentMethod = paymentMethod,
        chargingDayInTheMonth = paymentMethods.chargingDay,
        account = payinMethod.details?.toPaymentAccount(),
      )
    }
  }
}

internal data class MemberPaymentsDetails(
  val paymentMethod: PaymentMethod,
  val chargingDayInTheMonth: Int?,
  val account: PaymentAccount?,
)

private fun MemberPaymentProvider.toPaymentMethod(): PaymentMethod? = when (this) {
  MemberPaymentProvider.TRUSTLY -> PaymentMethod.TRUSTLY
  MemberPaymentProvider.SWISH -> PaymentMethod.SWISH
  MemberPaymentProvider.NORDEA -> PaymentMethod.NORDEA
  MemberPaymentProvider.INVOICE -> PaymentMethod.INVOICE
  MemberPaymentProvider.UNKNOWN__ -> null
}

private fun MemberPaymentMethodFragment.Details.toPaymentAccount(): PaymentAccount? = when (this) {
  is PaymentMethodInvoiceDetailsDetails -> when (delivery) {
    PaymentMethodInvoiceDelivery.KIVRA -> PaymentAccount.Kivra
    PaymentMethodInvoiceDelivery.MAIL -> email?.let(PaymentAccount::Email)
    PaymentMethodInvoiceDelivery.UNKNOWN__ -> null
  }

  is PaymentMethodSwishDetailsDetails -> PaymentAccount.PhoneNumber(phoneNumber)

  is PaymentMethodBankAccountDetailsDetails -> PaymentAccount.BankAccount(account, bank)

  else -> null
}
