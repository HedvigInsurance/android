package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.GetPayinMethodsQuery
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodBankAccountDetails
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodInvoiceDetails
import octopus.GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod.Details.Companion.asPaymentMethodSwishDetails
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

internal data class PayinAccountData(
  val currentMethods: List<PayinAccount>,
  val availablePayinMethods: List<MemberPaymentProvider>,
)

internal class GetPayinAccountUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, PayinAccountData> = either {
    logcat { "Mariia: GetPayinAccountUseCase launching" }
    val result = apolloClient
      .query(GetPayinMethodsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()

    val paymentMethods = result.currentMember.paymentMethods

    val currentMethods: List<PayinAccount> = paymentMethods.payinMethods.mapNotNull { method ->
      val isPending = method.status == MemberPaymentMethodStatus.PENDING
      when (method.provider) {
        MemberPaymentProvider.SWISH -> {
          val phoneNumber = method.details?.asPaymentMethodSwishDetails()?.phoneNumber
          PayinAccount.SwishPayin(
            phoneNumber = phoneNumber,
            isPending = isPending,
            isDefault = method.isDefault,
          )
        }

        MemberPaymentProvider.TRUSTLY -> {
          val (clearingNumber, accountNumber, bankName) = parseBankAccountDetails(method)
          PayinAccount.Trustly(
            clearingNumber = clearingNumber,
            accountNumber = accountNumber,
            bankName = bankName,
            isPending = isPending,
            isDefault = method.isDefault
          )
        }

        MemberPaymentProvider.INVOICE -> {
          val invoiceDetails = method.details?.asPaymentMethodInvoiceDetails()
          PayinAccount.Invoice(
            delivery = invoiceDetails?.delivery?.toDeliveryString(),
            email = invoiceDetails?.email,
            isPending = isPending,
            isDefault = method.isDefault
          )
        }

        else -> {
          null
        }
      }
    }

    val availablePayinMethods = paymentMethods.availableMethods
      .filter { it.supportsPayin }
      .map { it.provider }

    val finalResult = PayinAccountData(
      currentMethods = currentMethods,
      availablePayinMethods = availablePayinMethods,
    )
    logcat { "Mariia: GetPayinAccountUseCase finalResult: $finalResult" }
    finalResult
  }
}

private data class ParsedBankAccountDetails(
  val clearingNumber: String?,
  val accountNumber: String?,
  val bankName: String?,
)

private fun parseBankAccountDetails(
  method: GetPayinMethodsQuery.Data.CurrentMember.PaymentMethods.PayinMethod,
): ParsedBankAccountDetails {
  val bankAccountDetails = method.details?.asPaymentMethodBankAccountDetails()
  val account = bankAccountDetails?.account
  val dashIndex = account?.indexOf('-') ?: -1
  val clearingNumber = if (dashIndex >= 0) account?.substring(0, dashIndex) else account
  val accountNumber = if (dashIndex >= 0) account?.substring(dashIndex + 1) else null
  return ParsedBankAccountDetails(
    clearingNumber = clearingNumber,
    accountNumber = accountNumber,
    bankName = bankAccountDetails?.bank,
  )
}

internal sealed interface PayinAccount {
  val isPending: Boolean
  val isDefault: Boolean

  data class Trustly(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount

  data class SwishPayin(
    val phoneNumber: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount

  data class Invoice(
    val delivery: String?,
    val email: String?,
    override val isPending: Boolean,
    override val isDefault: Boolean,
  ) : PayinAccount
}

private fun PaymentMethodInvoiceDelivery?.toDeliveryString(): String? {
  return when(this) {
    PaymentMethodInvoiceDelivery.KIVRA -> "Kivra" //todo
    PaymentMethodInvoiceDelivery.MAIL -> "Email" //todo
    PaymentMethodInvoiceDelivery.UNKNOWN__ -> ""
    else -> null
  }
}
