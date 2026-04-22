package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.GetPayoutMethodsQuery
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.DefaultPayoutMethod.Details.Companion.asPaymentMethodBankAccountDetails
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.DefaultPayoutMethod.Details.Companion.asPaymentMethodInvoiceDetails
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.DefaultPayoutMethod.Details.Companion.asPaymentMethodSwishDetails
import octopus.type.MemberPaymentProvider

internal data class PayoutAccountData(
  val currentMethod: PayoutAccount?,
  val availablePayoutMethods: List<MemberPaymentProvider>,
)

internal class GetPayoutAccountUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, PayoutAccountData> = either {
    return@either PayoutAccountData(
      currentMethod = FakePayoutAccountStorage.currentMethod,
      availablePayoutMethods = listOf(
        MemberPaymentProvider.TRUSTLY,
        MemberPaymentProvider.NORDEA,
        MemberPaymentProvider.SWISH,
        MemberPaymentProvider.INVOICE,
      ),
    )
    val result = apolloClient
      .query(GetPayoutMethodsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()

    val paymentMethods = result.currentMember.paymentMethods
    val currentMethod = paymentMethods.defaultPayoutMethod?.let { method ->
      when (method.provider) {
        MemberPaymentProvider.TRUSTLY -> {
          PayoutAccount.Trustly
        }

        MemberPaymentProvider.SWISH -> {
          val swishDetails = method.details?.asPaymentMethodSwishDetails()
          if (swishDetails != null) {
            PayoutAccount.SwishPayout(phoneNumber = swishDetails.phoneNumber)
          } else {
            null
          }
        }

        MemberPaymentProvider.NORDEA -> {
          val bankAccountDetails = method.details?.asPaymentMethodBankAccountDetails()
          if (bankAccountDetails != null) {
            val account = bankAccountDetails.account
            val dashIndex = account.indexOf('-')
            val clearingNumber = if (dashIndex >= 0) account.substring(0, dashIndex) else account
            val accountNumber = if (dashIndex >= 0) account.substring(dashIndex + 1) else ""
            PayoutAccount.BankAccount(
              clearingNumber = clearingNumber,
              accountNumber = accountNumber,
              bankName = bankAccountDetails.bank,
            )
          } else {
            null
          }
        }

        MemberPaymentProvider.INVOICE -> {
          val invoiceDetails = method.details?.asPaymentMethodInvoiceDetails()
          if (invoiceDetails != null) {
            PayoutAccount.Invoice(
              delivery = invoiceDetails.delivery,
              email = invoiceDetails.email,
            )
          } else {
            null
          }
        }

        else -> {
          null
        }
      }
    }

    val availablePayoutMethods = paymentMethods.availableMethods
      .filter { it.supportsPayout }
      .map { it.provider }

    PayoutAccountData(
      currentMethod = currentMethod,
      availablePayoutMethods = availablePayoutMethods,
    )
  }
}
