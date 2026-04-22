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
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.PayoutMethod.Details.Companion.asPaymentMethodBankAccountDetails
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.PayoutMethod.Details.Companion.asPaymentMethodInvoiceDetails
import octopus.GetPayoutMethodsQuery.Data.CurrentMember.PaymentMethods.PayoutMethod.Details.Companion.asPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
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
    val defaultPayoutMethod = paymentMethods.payoutMethods.firstOrNull { it.isDefault }
    // todo payout feature. Return the other, non-default payout methods when we can switch to them
    val currentMethod = defaultPayoutMethod?.let { method ->
      val isPending = method.status == MemberPaymentMethodStatus.PENDING
      when (method.provider) {
        MemberPaymentProvider.TRUSTLY -> {
          PayoutAccount.Trustly(isPending = isPending)
        }

        MemberPaymentProvider.SWISH -> {
          val phoneNumber = method.details?.asPaymentMethodSwishDetails()?.phoneNumber
          PayoutAccount.SwishPayout(phoneNumber = phoneNumber, isPending = isPending)
        }

        MemberPaymentProvider.NORDEA -> {
          val bankAccountDetails = method.details?.asPaymentMethodBankAccountDetails()
          val account = bankAccountDetails?.account
          val dashIndex = account?.indexOf('-') ?: -1
          val clearingNumber = if (dashIndex >= 0) account?.substring(0, dashIndex) else account
          val accountNumber = if (dashIndex >= 0) account?.substring(dashIndex + 1) else null
          PayoutAccount.BankAccount(
            clearingNumber = clearingNumber,
            accountNumber = accountNumber,
            bankName = bankAccountDetails?.bank,
            isPending = isPending,
          )
        }

        MemberPaymentProvider.INVOICE -> {
          val invoiceDetails = method.details?.asPaymentMethodInvoiceDetails()
          PayoutAccount.Invoice(
            delivery = invoiceDetails?.delivery,
            email = invoiceDetails?.email,
            isPending = isPending,
          )
        }

        else -> null
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
